package pro.ycdl.chat.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pro.ycdl.chat.common.Constants;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class ChatGPTSseEventSourceListener extends EventSourceListener {

    private SseEmitter sseEmitter;
    private StringBuilder allData = new StringBuilder();
    private String question;
    private HttpServletRequest request;
    private Long start_time;
    private ChatCompletionResponse chatCompletionResponse;

    public ChatGPTSseEventSourceListener(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    @Override
    public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
        log.info("open-ai建立sse连接成功...");
    }

    @SneakyThrows
    @Override
    public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
        log.info("open-ai返回数据：" + data);
        if (Objects.equals(data, Constants.END_CODE)) {
            log.info("open-ai数据返回结束");
            sseEmitter.send(SseEmitter.event()
                    .id(Constants.END_CODE)
                    .data(Constants.END_CODE)
                    .reconnectTime(Constants.RECONNECT_TIME));
            return;
        }
        // 解析json
        ObjectMapper mapper = new ObjectMapper();
        ChatCompletionResponse response = mapper.readValue(data, ChatCompletionResponse.class);
        String content = response.getChoices().get(0).getDelta().getContent();
        if (!Objects.equals(content, "null") && content != null) {
            // 拼接结果，方便存储到数据库
            this.allData.append(content);
        }
        if (Objects.equals(response.getChoices().get(0).getFinishReason(), "stop") || Objects.equals(response.getChoices().get(0).getFinishReason(), "length")) {
            log.info(this.allData.toString());
            this.setChatCompletionResponse(response);
        }
        sseEmitter.send(SseEmitter.event()
                .id(response.getId())
                .data(response.getChoices().get(0).getDelta())
                .reconnectTime(Constants.RECONNECT_TIME));
    }

    @Override
    public void onClosed(@NotNull EventSource eventSource) {
        log.info("open-ai关闭sse连接" + this.sseEmitter);
        // 会触发 sseEmitter.onCompletion() 回调
        sseEmitter.complete();
    }

    @SneakyThrows
    @Override
    public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
        if (Objects.isNull(response)) {
            return;
        }
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
            log.error("open-ai sse连接异常data:{}, 异常{}", body.string(), t);
        } else {
            log.error("open-ai sse连接异常data:{}，异常:{}", response, t);
        }
        eventSource.cancel();
    }
}
