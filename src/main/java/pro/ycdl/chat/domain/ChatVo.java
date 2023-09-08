package pro.ycdl.chat.domain;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 返回前端的问答实体对象，只展示问题，结果，时间等。
 * @author pomelo
 *
 */
@Data
public class ChatVo implements Serializable {

    private String prompt;
    private String answer;
    private Integer isEnd;
    private String promptId;
    private Date createTime;
    private Double temperature;

    public ChatVo(@NotNull Chat chat) {
        this.prompt = chat.getChatPrompt();
        this.answer = chat.getChatAnswer();
        this.promptId = chat.getChatPromptId();
        this.isEnd = chat.getChatIsEnd();
        this.createTime = chat.getChatCreateTime();
        this.temperature = chat.getChatTemperature();
    }

    public static List<ChatVo> toChatVo(List<Chat> chatList) {
        return chatList.stream().map(ChatVo::new).toList();
    }


}
