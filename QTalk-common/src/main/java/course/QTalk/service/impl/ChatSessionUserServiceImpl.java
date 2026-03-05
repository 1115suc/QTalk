package course.QTalk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QTalk.pojo.po.ChatSessionUser;
import course.QTalk.service.ChatSessionUserService;
import course.QTalk.mapper.ChatSessionUserMapper;
import org.springframework.stereotype.Service;

/**
* @author 32147
* @description 针对表【chat_session_user(联系人表)】的数据库操作Service实现
* @createDate 2026-03-05 21:55:02
*/
@Service
public class ChatSessionUserServiceImpl extends ServiceImpl<ChatSessionUserMapper, ChatSessionUser>
    implements ChatSessionUserService{

}




