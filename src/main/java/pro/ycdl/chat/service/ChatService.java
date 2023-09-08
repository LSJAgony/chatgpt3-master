package pro.ycdl.chat.service;

import pro.ycdl.chat.domain.Chat;
import pro.ycdl.chat.domain.ChatVo;

import java.util.List;

public interface ChatService{

    int deleteByPrimaryKey(String chatId);

    int insert(Chat record);

    int insertSelective(Chat record);

    Chat selectByPrimaryKey(String chatId);

    int updateByPrimaryKeySelective(Chat record);

    int updateByPrimaryKey(Chat record);

    List<ChatVo> getPage(String uid, Integer pageSize, Integer pageNum);
}
