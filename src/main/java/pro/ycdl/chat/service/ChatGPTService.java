package pro.ycdl.chat.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface ChatGPTService {

    SseEmitter askGPT(String question, String uid, HttpServletRequest req) throws IOException;
}
