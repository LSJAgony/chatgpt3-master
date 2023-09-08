package pro.ycdl.chat.service.impl;

import cn.hutool.core.lang.ObjectId;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.ChatChoice;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pro.ycdl.chat.common.ChatGPTUtils;
import pro.ycdl.chat.common.Constants;
import pro.ycdl.chat.config.ChatConfig;
import pro.ycdl.chat.config.ChatGPTConfig;
import pro.ycdl.chat.domain.Chat;
import pro.ycdl.chat.domain.Prompt;
import pro.ycdl.chat.listener.ChatGPTSseEventSourceListener;
import pro.ycdl.chat.service.ChatGPTService;
import pro.ycdl.chat.service.ChatService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@Data
public class ChatGPTServiceImpl implements ChatGPTService {

    private OpenAiStreamClient client;
    private ChatGPTConfig chatGPTConfig;
    private Long start_time;

    @Resource
    private ChatConfig chatConfig;

    @Resource
    private ChatService chatService;

    @Resource
    private RedisTemplate<String, List<Chat>> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public SseEmitter askGPT(String question, String uid, HttpServletRequest request) throws IOException {
        log.info("question:" + question);
        String value = stringRedisTemplate.opsForValue().get("uid:" + uid);
        if (value == null || value.isBlank()) {
            stringRedisTemplate.opsForValue().set("uid:" + uid, uid);
        }
        this.setStart_time(System.currentTimeMillis());
        SseEmitter sseEmitter = new SseEmitter(0L);
        if (client == null) {
            OkHttpClient okHttpClient = new OkHttpClient
                    .Builder()
                    .proxy(chatConfig.getProxyPort() == null ? null : new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", chatConfig.getProxyPort())))//自定义代理
                    .connectTimeout(chatConfig.getTimeOut(), TimeUnit.SECONDS) // 自定义超时时间
                    .writeTimeout(chatConfig.getWriteTimeOut(), TimeUnit.SECONDS) // 自定义超时时间
                    .readTimeout(chatConfig.getReadTimeOut(), TimeUnit.SECONDS) // 自定义超时时间
                    .build();
            client = OpenAiStreamClient.builder()
                    .apiKey(Collections.singletonList(chatConfig.getApiKey()))
                    .apiHost(Constants.API_HOST)
                    .okHttpClient(okHttpClient)
                    .build();
            chatGPTConfig = chatConfig.getChatGPT();
        }
        ChatGPTSseEventSourceListener sourceListener = new ChatGPTSseEventSourceListener(sseEmitter);
        sourceListener.setQuestion(question);
        sourceListener.setRequest(request);
        sseEmitter.send(SseEmitter.event().
                id("GaoYang")
                .name("连接成功")
                .data(LocalDateTime.now())
                .reconnectTime(Constants.RECONNECT_TIME));
        sseEmitter.onCompletion(() -> {
            Prompt prompt = new Prompt(question, ChatGPTUtils.getIpAddress(request), sourceListener.getAllData().toString(), uid, (int) (System.currentTimeMillis() - this.start_time));
            Chat chat = insert(sourceListener.getChatCompletionResponse(), prompt);
            if (chat == null) {
                log.info("数据库插入失败");
            } else {
                log.info("数据库插入成功");
                // 获取redis中的存储的聊天信息
                List<Chat> list = redisTemplate.opsForValue().get(Constants.REDIS_KEY);
                if (list != null && list.size() > 0) {
                    ArrayList<Chat> chats = new ArrayList<>(list);
                    if (chats.size() == chatConfig.getSize()) {
                        chats.remove(0);
                    }
                    chats.add(chat);
                    redisTemplate.opsForValue().set(Constants.REDIS_KEY, chats, Constants.REDIS_EXPIRE_TIME, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set(Constants.REDIS_KEY, Collections.singletonList(chat), Constants.REDIS_EXPIRE_TIME, TimeUnit.SECONDS);
                }
                log.info("redis插入成功");
            }
        });
        sseEmitter.onError(throwable -> {
            try {
                log.info(LocalDateTime.now() + ", uid#" + "765431" + ", on error#" + throwable.toString());
                sseEmitter.send(SseEmitter.event().id("765431").name("发生异常！").data(throwable.getMessage()).reconnectTime(3000));
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
        List<Message> messages = new ArrayList<>();
        List<Chat> chatList = redisTemplate.opsForValue().get(Constants.REDIS_KEY);
        if (chatList != null && chatList.size() > 0) {
            ArrayList<Chat> chats = new ArrayList<>(chatList);
            chats.forEach(ch -> {
                messages.add(Message.builder().content(ch.getChatPrompt()).role(Message.Role.USER).build());
                messages.add(Message.builder().content(ch.getChatAnswer()).role(Message.Role.ASSISTANT).build());
            });
        }

        messages.add(Message.builder().content(question).role(Message.Role.USER).build());
        // 判断历史信息的长度
        AtomicInteger size = new AtomicInteger();
        Iterator<Message> iterator = messages.iterator();
        for (int i = messages.size() - 1; i > 0; i--) {
            Message message = messages.get(i);
            size.addAndGet(message.getContent().length());
            if (size.get() > chatGPTConfig.getMaxTokens() / 2) {
                // 如果是用户信息，就删除
                if (message.getRole().equalsIgnoreCase(Message.Role.ASSISTANT.getName())) {
                    // 减掉删除的信息的长度
                    messages.remove(i);
                    messages.remove(i - 1);
                    size.addAndGet((-1) * message.getContent().length());
                } else {
                    // 减掉删除的信息的长度
                    messages.remove(i);
                    messages.remove(i + 1);
                    size.addAndGet((-1) * message.getContent().length());
                }
            }
        }
//        while (iterator.hasNext()) {
//            Message next = iterator.next();
//            size.addAndGet(next.getContent().length());
//            if (size.get() > chatGPTConfig.getMaxTokens() / 2) {
//                // 如果是用户信息，就删除
//                if (next.getRole().equalsIgnoreCase(Message.Role.USER.getName())) {
//                    iterator.remove();
//                    // 减掉删除的信息的长度
//                    size.addAndGet((-1) * next.getContent().length());
//                } else {
//                    break;
//                }
//            }
//        }
        ChatCompletion chatCompletion = ChatCompletion.builder().model(chatGPTConfig.getModel()).messages(messages).build();
        client.streamChatCompletion(chatCompletion, sourceListener);
        return sseEmitter;
    }

    private Chat insert(@NotNull ChatCompletionResponse response, @NotNull Prompt prompt) {
        ChatChoice chatChoice = response.getChoices().get(0);
        chatChoice.getDelta().setContent(prompt.getData());
        Chat chat = new Chat();
        chat.setChatId(ObjectId.next());
        chat.setChatUserKey(prompt.getUid());
        chat.setChatUsername("游客");
        chat.setChatApiKey(chatConfig.getApiKey());
        chat.setChatModel(response.getModel());
        chat.setChatPrompt(prompt.getQuestion());
        chat.setChatAnswer(prompt.getData());
        chat.setChatIsEnd("stop".equals(chatChoice.getFinishReason()) ? 1 : 0);
        chat.setChatResponseJson(JSON.toJSONString(response));
        List<Chat> chatList = redisTemplate.opsForValue().get(Constants.REDIS_KEY);
        ObjectMapper mapper = new ObjectMapper();
        try {
            // 如果是联系上下文 记录上一个问题的 id
            chat.setChatPromptId(chatList == null || chatList.size() == 0 ? response.getId() : mapper.readValue(chatList.get(0).getChatResponseJson(), ChatCompletionResponse.class).getId());
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        chat.setChatRequestIpAddress(prompt.getIp());
        chat.setChatRequestUrl(Constants.REQUEST_URL);
        chat.setChatCreateTime(new Date());
        chat.setChatRequestTime(prompt.getRequestTime());
        chat.setChatMaxTokens(chatGPTConfig.getMaxTokens());
        chat.setChatTemperature(chatGPTConfig.getTemperature());
        chat.setChatThreadName(Thread.currentThread().getName());
        int insert = chatService.insert(chat);
        if (insert > 0) {
            return chat;
        } else {
            return null;
        }
    }
}
