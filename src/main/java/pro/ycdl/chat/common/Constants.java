package pro.ycdl.chat.common;

/**
 * 常量
 */
public class Constants {
    /**
     * 返回结果正确代码
     */
    public static final Integer SUCCESS_CODE = 1;
    /**
     * 返回结果错误代码
     */
    public static final Integer FAILURE_CODE = 0;

    /**
     * 请求的地址
     */
    public static final String REQUEST_URL = "https://api.openai.com/v1/completions";

    public static final String API_HOST = "https://api2.ycdl.pro/";

    public static final Integer RECONNECT_TIME = 3000;
    public static final String END_CODE = "[DONE]";

    public static final String REDIS_KEY = "chat-gpt:";

    public static final Long REDIS_EXPIRE_TIME = 60 * 60L;

}
