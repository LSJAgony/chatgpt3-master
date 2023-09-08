package pro.ycdl.chat.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "open-ai", ignoreInvalidFields = true)
@Component
@Data
public class ChatConfig {

    private String apiKey;
    private Long timeOut;
    private Long writeTimeOut;
    private Long readTimeOut;
    private Integer proxyPort;
    private ChatGPTConfig chatGPT;
    // 默认携带的历史消息条数
    private Long size = 2L;
}
