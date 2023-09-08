package pro.ycdl.chat.common;


import javax.servlet.http.HttpServletRequest;

/**
 * 工具类
 */
public class ChatGPTUtils {

    public static String getIpAddress(HttpServletRequest request) {
        if (request.getHeader("X-Forwarded-For") != null) {
            return request.getHeader("X-Forwarded-For");
        } else if (request.getHeader("X-Real-IP") != null) {
            return request.getHeader("X-Real-IP");
        } else {
            return "0:0:0:0:0:0:0:1".equals(request.getRemoteAddr()) ? "127.0.0.1" : request.getRemoteAddr();
        }
    }
}
