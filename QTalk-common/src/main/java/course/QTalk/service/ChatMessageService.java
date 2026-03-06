package course.QTalk.service;

import course.QTalk.pojo.dto.MessageSendDto;
import course.QTalk.pojo.po.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import course.QTalk.pojo.vo.request.SendMessageVO;
import course.QTalk.pojo.vo.response.R;

/**
* @author 32147
* @description 针对表【chat_message(消息表)】的数据库操作Service
* @createDate 2026-03-04 22:20:18
*/
public interface ChatMessageService extends IService<ChatMessage> {
    // 发送消息
    R<MessageSendDto> sendMessage(String token, String loginType, SendMessageVO sendMessageVO);
}
