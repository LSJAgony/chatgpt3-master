package pro.ycdl.chat.config;

import lombok.Data;

@Data
public class ChatGPTConfig {

    /**
     * 模型id 默认为 GPT-3.5-TURBO 取值参考：https://platform.openai.com/docs/models
     */
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private Integer topP;

}
