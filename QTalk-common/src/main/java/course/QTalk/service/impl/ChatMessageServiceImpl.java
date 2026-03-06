package course.QTalk.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QTalk.constant.CommonConstant;
import course.QTalk.exception.QTWebException;
import course.QTalk.mapper.ChatSessionUserMapper;
import course.QTalk.mapper.QtGroupMapper;
import course.QTalk.mapper.SysUserMapper;
import course.QTalk.pojo.dto.MessageSendDto;
import course.QTalk.pojo.dto.TokenUserDTO;
import course.QTalk.pojo.enums.ContactType;
import course.QTalk.pojo.enums.MessageTypeEnum;
import course.QTalk.pojo.enums.ResponseCode;
import course.QTalk.pojo.po.ChatMessage;
import course.QTalk.pojo.po.ChatSessionUser;
import course.QTalk.pojo.po.QtGroup;
import course.QTalk.pojo.po.SysUser;
import course.QTalk.pojo.vo.request.SendMessageVO;
import course.QTalk.pojo.vo.response.R;
import course.QTalk.service.ChatMessageService;
import course.QTalk.mapper.ChatMessageMapper;
import course.QTalk.util.RedisComponent;
import course.QTalk.util.ToolUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 32147
 * @description 针对表【chat_message(消息表)】的数据库操作Service实现
 * @createDate 2026-03-04 22:20:18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {

    private final RedisComponent redisComponent;
    private final SysUserMapper sysUserMapper;
    private final QtGroupMapper qtGroupMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionUserMapper chatSessionUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<MessageSendDto> sendMessage(String token, String loginType, SendMessageVO sendMessageVO) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(loginType, token);

        // 验证消息类型
        Integer messageType = sendMessageVO.getMessageType();
        if (!(NumberUtil.equals(messageType, MessageTypeEnum.CHAT.getType()) || NumberUtil.equals(messageType, MessageTypeEnum.MEDIA_CHAT.getType()))) {
            throw new QTWebException(ResponseCode.PARAM_ERROR.getMessage());
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSendUserId(tokenUserDTO.getUid());
        chatMessage.setSendUserNickname(tokenUserDTO.getNickname());
        chatMessage.setContactId(sendMessageVO.getContactId());
        chatMessage.setMessageContent(sendMessageVO.getMessageContent());
        chatMessage.setMessageType(messageType);
        chatMessage.setFileSize(sendMessageVO.getFileSize());
        chatMessage.setFileName(sendMessageVO.getFileName());
        chatMessage.setFileType(sendMessageVO.getFileType());

        String session = null;
        ContactType byPrefix = ContactType.getByPrefix(sendMessageVO.getContactId());
        MessageSendDto messageSendDto = new MessageSendDto();
        switch (byPrefix) {
            case USER:
                List<Object> contactUserList = redisComponent.getContactUserList(tokenUserDTO.getUid());
                if (CollectionUtil.isEmpty(contactUserList) || !contactUserList.contains(sendMessageVO.getContactId())) {
                    log.debug("用户不存在：{}", sendMessageVO.getContactId());
                    throw new QTWebException(ResponseCode.CODE_902.getMessage());
                }
                session = ToolUtils.getUserChatSession(tokenUserDTO.getUid(), sendMessageVO.getContactId());
                chatMessage.setSessionId(session);
                chatMessage.setContactType(ContactType.USER.getCode());
                messageSendDto = handlerMessage(chatMessage, byPrefix);
                break;
            case GROUP:
                List<Object> contactGroupList = redisComponent.getContactGroupList(tokenUserDTO.getUid());
                if (CollectionUtil.isEmpty(contactGroupList) || !contactGroupList.contains(sendMessageVO.getContactId())) {
                    log.debug("群组不存在：{}", sendMessageVO.getContactId());
                    throw new QTWebException(ResponseCode.CODE_903.getMessage());
                }
                session = ToolUtils.getGroupChatSession(sendMessageVO.getContactId());
                chatMessage.setSessionId(session);
                chatMessage.setContactType(ContactType.GROUP.getCode());
                messageSendDto = handlerMessage(chatMessage, byPrefix);
                break;
            default:
                log.error("联系人Id格式错误：{}", sendMessageVO.getContactId());
                throw new QTWebException(ResponseCode.PARAM_ERROR.getMessage());
        }

        return R.ok(messageSendDto);
    }

    public MessageSendDto handlerMessage(ChatMessage chatMessage, ContactType type) {
        MessageSendDto messageSendDto = new MessageSendDto<>();

        long currentTimeMillis = System.currentTimeMillis();
        chatMessage.setSendTime(currentTimeMillis);

        Integer messageType = chatMessage.getMessageType();
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(messageType);
        switch (messageTypeEnum) {
            case CHAT -> chatMessage.setStatus(CommonConstant.ONE);
            case MEDIA_CHAT -> chatMessage.setStatus(CommonConstant.ZERO);
            default -> throw new QTWebException(ResponseCode.PARAM_ERROR.getMessage());
        }

        String messageContent = ToolUtils.cleanHtmlTag(chatMessage.getMessageContent());
        chatMessage.setMessageContent(messageContent);

        LambdaQueryWrapper<ChatSessionUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSessionUser::getSessionId, chatMessage.getSessionId());
        List<ChatSessionUser> chatSessionUsers = chatSessionUserMapper.selectList(queryWrapper);

        // 不存在则添加会话
        if (CollectionUtil.isEmpty(chatSessionUsers)) {
            List<ChatSessionUser> chatSession = new ArrayList<>();

            ChatSessionUser ownChatSessionUser = new ChatSessionUser();
            ownChatSessionUser.setSessionId(chatMessage.getSessionId());
            ownChatSessionUser.setUid(chatMessage.getSendUserId());
            ownChatSessionUser.setContactId(chatMessage.getContactId());
            ownChatSessionUser.setLastMessage(messageContent);
            ownChatSessionUser.setLastReceiveTime(currentTimeMillis);

            switch (type) {
                case USER -> {
                    ChatSessionUser friendChatSessionUser = new ChatSessionUser();
                    friendChatSessionUser.setSessionId(chatMessage.getSessionId());
                    friendChatSessionUser.setUid(chatMessage.getContactId());
                    friendChatSessionUser.setContactId(chatMessage.getSendUserId());
                    friendChatSessionUser.setLastMessage(messageContent);
                    friendChatSessionUser.setLastReceiveTime(currentTimeMillis);

                    LambdaQueryWrapper<SysUser> queryWrapper1 = new LambdaQueryWrapper<>();
                    queryWrapper1.eq(SysUser::getUid, chatMessage.getSendUserId());
                    SysUser sysUser1 = sysUserMapper.selectOne(queryWrapper1);

                    LambdaQueryWrapper<SysUser> queryWrapper2 = new LambdaQueryWrapper<>();
                    queryWrapper2.eq(SysUser::getUid, chatMessage.getContactId());
                    SysUser sysUser2 = sysUserMapper.selectOne(queryWrapper2);

                    ownChatSessionUser.setContactName(sysUser2.getNickName());
                    friendChatSessionUser.setContactName(sysUser1.getNickName());

                    chatSession.add(friendChatSessionUser);
                }
                case GROUP -> {
                    LambdaQueryWrapper<QtGroup> queryWrapper1 = new LambdaQueryWrapper<>();
                    queryWrapper1.eq(QtGroup::getGroupId, chatMessage.getContactId());
                    QtGroup qtGroup = qtGroupMapper.selectOne(queryWrapper1);
                    ownChatSessionUser.setContactName(qtGroup.getName());
                }
            }

            chatSession.add(ownChatSessionUser);

            chatSessionUserMapper.insertOrUpdate(chatSession);
        }

        List<ChatSessionUser> collect = chatSessionUsers.stream().map(chatSessionUser -> {
            chatSessionUser.setLastMessage(messageContent);
            chatSessionUser.setLastReceiveTime(currentTimeMillis);
            return chatSessionUser;
        }).collect(Collectors.toList());

        chatSessionUserMapper.insertOrUpdate(collect);
        chatMessageMapper.insertOrUpdate(chatMessage);



        List<ChatMessage> chatMessages = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSendTime, chatMessage.getSendTime()));

        messageSendDto.setMessageId(chatMessages.get(0).getMessageId());
        messageSendDto.setSessionId(chatMessages.get(0).getSessionId());
        messageSendDto.setSendUserId(chatMessages.get(0).getSendUserId());
        messageSendDto.setSendUserNickName(chatMessages.get(0).getSendUserNickname());
        messageSendDto.setContactId(chatMessages.get(0).getContactId());
        messageSendDto.setMessageContent(chatMessages.get(0).getMessageContent());
        messageSendDto.setMessageType(chatMessages.get(0).getMessageType());
        messageSendDto.setSendTime(chatMessages.get(0).getSendTime());
        messageSendDto.setContactType(chatMessages.get(0).getContactType());
        messageSendDto.setStatus(chatMessages.get(0).getStatus());
        messageSendDto.setFileSize(chatMessages.get(0).getFileSize());
        messageSendDto.setFileName(chatMessages.get(0).getFileName());
        messageSendDto.setFileType(chatMessages.get(0).getFileType());
        log.debug("发送消息：{}", messageSendDto);
        return messageSendDto;
    }

}




