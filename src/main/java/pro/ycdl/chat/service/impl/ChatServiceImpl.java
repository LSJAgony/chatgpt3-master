package pro.ycdl.chat.service.impl;

import org.springframework.stereotype.Service;
import pro.ycdl.chat.domain.Chat;
import pro.ycdl.chat.domain.ChatVo;
import pro.ycdl.chat.mapper.ChatMapper;
import pro.ycdl.chat.service.ChatService;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService{

    @Resource
    private ChatMapper chatMapper;

    @Override
    public int deleteByPrimaryKey(String chatId) {
        return chatMapper.deleteByPrimaryKey(chatId);
    }

    @Override
    public int insert(Chat record) {
        return chatMapper.insert(record);
    }

    @Override
    public int insertSelective(Chat record) {
        return chatMapper.insertSelective(record);
    }

    @Override
    public Chat selectByPrimaryKey(String chatId) {
        return chatMapper.selectByPrimaryKey(chatId);
    }

    @Override
    public int updateByPrimaryKeySelective(Chat record) {
        return chatMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Chat record) {
        return chatMapper.updateByPrimaryKey(record);
    }

    @Override
    public List<ChatVo> getPage(String uid, Integer pageSize, Integer pageNum) {
        if (pageSize == null) {
            pageSize = 10;
        }
        if (pageNum == null) {
            pageNum = 0;
        }
        Integer start = pageNum * pageSize;
        List<Chat> chats = chatMapper.selectPage(uid, start, pageSize);
        if (chats == null) {
            return null;
        }
        return ChatVo.toChatVo(chats);
    }
}
