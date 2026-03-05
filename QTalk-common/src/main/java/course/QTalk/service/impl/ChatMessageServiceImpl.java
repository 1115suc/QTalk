package course.QTalk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QTalk.pojo.po.ChatMessage;
import course.QTalk.service.ChatMessageService;
import course.QTalk.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author 32147
* @description 针对表【chat_message(消息表)】的数据库操作Service实现
* @createDate 2026-03-04 22:20:18
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService{

}




