package pro.ycdl.chat.mapper;

import org.apache.ibatis.annotations.Mapper;
import pro.ycdl.chat.domain.Chat;

import java.util.List;

@Mapper
public interface ChatMapper {
    int deleteByPrimaryKey(String chatId);

    int insert(Chat record);

    int insertSelective(Chat record);

    Chat selectByPrimaryKey(String chatId);

    int updateByPrimaryKeySelective(Chat record);

    int updateByPrimaryKey(Chat record);

    List<Chat> selectPage(String uid, Integer start, Integer pageSize);
}