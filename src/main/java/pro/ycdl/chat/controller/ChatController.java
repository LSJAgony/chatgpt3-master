package pro.ycdl.chat.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pro.ycdl.chat.domain.AjaxResult;
import pro.ycdl.chat.domain.ChatVo;
import pro.ycdl.chat.service.ChatGPTService;
import pro.ycdl.chat.service.ChatService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/open-ai")
public class ChatController {

    @Resource
    private ChatGPTService chatGPTService;

    @Resource
    private ChatService chatService;

    @GetMapping("/chat")
    public SseEmitter chat(@RequestParam("question")String question, @RequestParam("uid") String uid, HttpServletRequest request) throws IOException {
        return chatGPTService.askGPT(question, uid, request);
    }

    /**
     * 查询历史记录
     * @param pageSize 查询记录条数
     * @param pageNum 查询记录页数
     */
    @GetMapping("/history")
    @ResponseBody
    public AjaxResult history(String uid, Integer pageSize, Integer pageNum) {
        if (uid == null) {
            return AjaxResult.failure("uid不能为空");
        }
        List<ChatVo> list = chatService.getPage(uid, pageSize, pageNum);
        return list == null ? AjaxResult.failure("查询失败") : AjaxResult.success(list);
    }


}
