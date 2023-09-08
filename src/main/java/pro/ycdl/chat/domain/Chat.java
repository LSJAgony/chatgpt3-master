package pro.ycdl.chat.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class Chat implements Serializable {
    private String chatId;

    /**
     * 用户名
     */
    private String chatUsername;

    /**
     * 用户提问的key kfm-开头
     */
    private String chatUserKey;

    /**
     * apikey
     */
    private String chatApiKey;

    /**
     * 使用的model类型
     */
    private String chatModel;

    /**
     * 问题
     */
    private String chatPrompt;

    /**
     * 答案
     */
    private String chatAnswer;

    /**
     * 是否结束
     */
    private Integer chatIsEnd;

    /**
     * 访问的ip地址
     */
    private String chatRequestIpAddress;

    /**
     * 返回的response JSON串
     */
    private String chatResponseJson;

    private String chatPromptId;

    /**
     * 问答结束的原因
     */
    private String chatFinishReason;

    /**
     * 请求的url
     */
    private String chatRequestUrl;

    /**
     * 请求创建的时间
     */
    private Date chatCreateTime;

    /**
     * 请求完成的时间，单位s
     */
    private Integer chatRequestTime;

    /**
     * 请求的最大字符数量
     */
    private Integer chatMaxTokens;

    /**
     * 请求的温度
     */
    private Double chatTemperature;

    /**
     * 运行的线程名
     */
    private String chatThreadName;

    private static final long serialVersionUID = 1L;
}