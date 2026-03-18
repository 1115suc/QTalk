package course.QTalk.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QTalk.constant.CommonConstant;
import course.QTalk.exception.QTWebException;
import course.QTalk.handler.MessageTopicHandler;
import course.QTalk.mapper.ChatMessageMapper;
import course.QTalk.mapper.ChatSessionUserMapper;
import course.QTalk.mapper.QtFriendMapper;
import course.QTalk.mapper.QtGroupMapper;
import course.QTalk.mapper.QtGroupMemberMapper;
import course.QTalk.mapper.SysUserMapper;
import course.QTalk.pojo.bo.LoadPendingBo;
import course.QTalk.pojo.dto.MessageSendDto;
import course.QTalk.pojo.dto.TokenUserDTO;
import course.QTalk.pojo.enums.AddFriendsEnum;
import course.QTalk.pojo.enums.ApplyStatus;
import course.QTalk.pojo.enums.ContactType;
import course.QTalk.pojo.enums.DeletedEnum;
import course.QTalk.pojo.enums.FriendStatus;
import course.QTalk.pojo.enums.GroupRole;
import course.QTalk.pojo.enums.GroupStatus;
import course.QTalk.pojo.enums.MessageTypeEnum;
import course.QTalk.pojo.enums.ResponseCode;
import course.QTalk.pojo.enums.StatusEnum;
import course.QTalk.pojo.po.ChatMessage;
import course.QTalk.pojo.po.ChatSessionUser;
import course.QTalk.pojo.po.QtContactRequest;
import course.QTalk.pojo.po.QtFriend;
import course.QTalk.pojo.po.QtGroup;
import course.QTalk.pojo.po.QtGroupMember;
import course.QTalk.pojo.po.SysUser;
import course.QTalk.pojo.vo.request.ApplyJoinContactVO;
import course.QTalk.pojo.vo.request.GroupBasicInfoVO;
import course.QTalk.pojo.vo.request.HandleFormApplyVO;
import course.QTalk.pojo.vo.request.LoadPendingRequestsVO;
import course.QTalk.pojo.vo.request.UserSearchVO;
import course.QTalk.pojo.vo.response.GroupInfoVO;
import course.QTalk.pojo.vo.response.LoadPendingResponseVO;
import course.QTalk.pojo.vo.response.R;
import course.QTalk.pojo.vo.response.UserSearchInfoVO;
import course.QTalk.service.QtContactRequestService;
import course.QTalk.mapper.QtContactRequestMapper;
import course.QTalk.handler.RedisComponent;
import course.QTalk.util.RedisUtil;
import course.QTalk.util.ToolUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 32147
 * @description 针对表【qt_contact_request(QT联系人申请表)】的数据库操作Service实现
 * @createDate 2026-02-28 11:32:12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QtContactRequestServiceImpl extends ServiceImpl<QtContactRequestMapper, QtContactRequest>
        implements QtContactRequestService {

    private final RedisUtil redisUtil;
    private final RedisComponent redisComponent;
    private final MessageTopicHandler messageTopicHandler;
    private final SysUserMapper sysUserMapper;
    private final QtGroupMapper qtGroupMapper;
    private final QtGroupMemberMapper qtGroupMemberMapper;
    private final QtFriendMapper qtFriendMapper;
    private final QtContactRequestMapper qtContactRequestMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionUserMapper chatSessionUserMapper;

    @Override
    public R<List<UserSearchInfoVO>> searchUser(UserSearchVO userSearchVO) {
        String uid = userSearchVO.getUid();
        String nickName = userSearchVO.getNickName();
        String email = userSearchVO.getEmail();

        if (StrUtil.isBlank(uid) && StrUtil.isBlank(nickName) && StrUtil.isBlank(email)) {
            return R.ok(ResponseCode.PARAM_NOT_EMPTY.getMessage());
        }

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        // 过滤已删除用户
        queryWrapper.eq(SysUser::getDeleted, DeletedEnum.NOT_DELETED.getCode());

        // 构造搜索条件: uid精确匹配 OR email精确匹配 OR nickName模糊匹配
        queryWrapper.and(wrapper -> wrapper
                .eq(SysUser::getUid, uid)
                .or()
                .eq(SysUser::getEmail, email)
                .or()
                .like(SysUser::getNickName, nickName)
        );

        List<SysUser> sysUsers = sysUserMapper.selectList(queryWrapper);

        if (CollectionUtil.isEmpty(sysUsers)) {
            return R.ok(ResponseCode.CONTACT_NOT_EXISTS.getMessage());
        }

        List<UserSearchInfoVO> userSearchInfoVOS = sysUsers.stream().map(sysUser -> {
            UserSearchInfoVO vo = new UserSearchInfoVO();
            BeanUtil.copyProperties(sysUser, vo);
            return vo;
        }).collect(Collectors.toList());

        return R.ok(userSearchInfoVOS);
    }

    @Override
    public R<List<GroupInfoVO>> queryGroupInfo(GroupBasicInfoVO groupBasicInfoVO) {
        if (StrUtil.isBlank(groupBasicInfoVO.getGroupId()) && StrUtil.isBlank(groupBasicInfoVO.getName())) {
            throw new QTWebException(ResponseCode.GROUP_ID_OR_NAME_EMPTY.getMessage(), ResponseCode.GROUP_ID_OR_NAME_EMPTY.getCode());
        }

        LambdaQueryWrapper<QtGroup> queryWrapper = new LambdaQueryWrapper<>();
        // 过滤状态为正常的群组
        queryWrapper.eq(QtGroup::getStatus, GroupStatus.NORMAL.getCode());

        queryWrapper.like(StrUtil.isNotBlank(groupBasicInfoVO.getGroupId()), QtGroup::getGroupId, groupBasicInfoVO.getGroupId());
        queryWrapper.like(StrUtil.isNotBlank(groupBasicInfoVO.getName()), QtGroup::getName, groupBasicInfoVO.getName());

        List<QtGroup> qtGroups = qtGroupMapper.selectList(queryWrapper);

        if (CollectionUtil.isNotEmpty(qtGroups)) {
            List<GroupInfoVO> groupInfoVOList = qtGroups.stream().map(qtGroup -> {
                GroupInfoVO groupInfoVO = new GroupInfoVO();
                BeanUtil.copyProperties(qtGroup, groupInfoVO);
                return groupInfoVO;
            }).collect(Collectors.toList());

            return R.ok(groupInfoVOList);
        }

        return R.error(ResponseCode.GROUP_NOT_EXISTS.getCode(), ResponseCode.GROUP_NOT_EXISTS.getMessage());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyAddFriend(String token, String type, ApplyJoinContactVO applyJoinContactVO) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(type, token);

        String applyId = applyJoinContactVO.getApplyId();
        if (!applyId.startsWith("U")) {
            throw new QTWebException(ResponseCode.USER_ID_ERROR.getMessage());
        }

        String fromUid = tokenUserDTO.getUid();
        String toUid = applyJoinContactVO.getApplyId();

        // 1. 校验目标用户是否存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUid, toUid);
        queryWrapper.eq(SysUser::getDeleted, DeletedEnum.NOT_DELETED.getCode());
        SysUser toUser = sysUserMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNull(toUser)) {
            throw new QTWebException(ResponseCode.ACCOUNT_NOT_EXISTS.getMessage());
        }

        // 2. 校验是否允许加好友
        if (toUser.getAddFriends().equals(AddFriendsEnum.NOT_ALLOW_ADD_FRIEND.getCode())) {
            throw new QTWebException("对方设置了不允许添加好友");
        }

        // 3. 校验是否已经是好友
        LambdaQueryWrapper<QtFriend> friendQuery = new LambdaQueryWrapper<>();
        friendQuery.eq(QtFriend::getUserUid, fromUid);
        friendQuery.eq(QtFriend::getFriendUid, toUid);
        QtFriend friend = qtFriendMapper.selectOne(friendQuery);
        if (ObjectUtil.isNotNull(friend) && friend.getStatus().equals(CommonConstant.ZERO)) {
            throw new QTWebException(ResponseCode.ALREADY_FRIEND.getMessage());
        }

        // 4. 检查是否已经申请过且未处理
        LambdaQueryWrapper<QtContactRequest> requestQuery = new LambdaQueryWrapper<>();
        requestQuery.eq(QtContactRequest::getFromUid, fromUid);
        requestQuery.eq(QtContactRequest::getToId, toUid);
        requestQuery.eq(QtContactRequest::getToType, ContactType.USER.getCode());
        requestQuery.eq(QtContactRequest::getStatus, ApplyStatus.PENDING.getCode());
        Long count = qtContactRequestMapper.selectCount(requestQuery);
        if (count > 0) {
            throw new QTWebException(ResponseCode.DUPLICATE_REQUEST.getMessage());
        }

        // 5. 构造申请理由
        String reason = applyJoinContactVO.getApplyReason();
        if (StrUtil.isBlank(reason)) {
            reason = String.format(CommonConstant.APPLY_REASON_TEMPLATE, tokenUserDTO.getNickname());
        }

        // 6. 插入申请记录
        QtContactRequest contactRequest = new QtContactRequest();
        contactRequest.setFromUid(fromUid);
        contactRequest.setToId(toUid);
        contactRequest.setToType(ContactType.USER.getCode());
        contactRequest.setContactId(toUid); // 对于好友申请，contactId即为对方UID
        contactRequest.setReason(reason);
        contactRequest.setStatus(ApplyStatus.PENDING.getCode());
        contactRequest.setCreateTime(new Date());

        qtContactRequestMapper.insert(contactRequest);

        // TODO 发送消息
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setContactId(toUid);
        messageSendDto.setMessageContent(reason);
        messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
        messageTopicHandler.sendMessage(messageSendDto);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyJoinGroup(String token, String type, ApplyJoinContactVO applyJoinContactVO) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(type, token);

        String fromUid = tokenUserDTO.getUid();
        String groupId = applyJoinContactVO.getApplyId();

        String applyId = applyJoinContactVO.getApplyId();
        if (!applyId.startsWith("Q")) {
            throw new QTWebException(ResponseCode.GROUP_ID_ERROR.getMessage());
        }

        // 1. 校验群组是否存在且正常
        LambdaQueryWrapper<QtGroup> groupQuery = new LambdaQueryWrapper<>();
        groupQuery.eq(QtGroup::getGroupId, groupId);
        groupQuery.eq(QtGroup::getStatus, GroupStatus.NORMAL.getCode());
        QtGroup qtGroup = qtGroupMapper.selectOne(groupQuery);
        if (ObjectUtil.isNull(qtGroup)) {
            throw new QTWebException(ResponseCode.GROUP_NOT_EXISTS.getMessage());
        }

        // 2. 校验入群方式
        Integer joinType = qtGroup.getJoinType();

        // 同意后加入
        if (joinType.equals(CommonConstant.ZERO)) { // 2:需要群主或管理员同意
            // 校验是否已经在群组中
            LambdaQueryWrapper<QtGroupMember> memberQuery = new LambdaQueryWrapper<>();
            memberQuery.eq(QtGroupMember::getGroupId, groupId);
            memberQuery.eq(QtGroupMember::getUserUid, fromUid);
            memberQuery.eq(QtGroupMember::getIsQuit, CommonConstant.ZERO);
            Long memberCount = qtGroupMemberMapper.selectCount(memberQuery);
            if (memberCount > 0) {
                throw new QTWebException("您已在该群组中");
            }

            // 检查是否已经申请过且未处理
            LambdaQueryWrapper<QtContactRequest> requestQuery = new LambdaQueryWrapper<>();
            requestQuery.eq(QtContactRequest::getFromUid, fromUid);
            requestQuery.eq(QtContactRequest::getToId, groupId);
            requestQuery.eq(QtContactRequest::getToType, ContactType.GROUP.getCode());
            requestQuery.eq(QtContactRequest::getStatus, ApplyStatus.PENDING.getCode());
            Long count = qtContactRequestMapper.selectCount(requestQuery);
            if (count > 0) {
                throw new QTWebException(ResponseCode.DUPLICATE_REQUEST.getMessage());
            }

            // 构造申请理由
            String reason = applyJoinContactVO.getApplyReason();
            if (StrUtil.isBlank(reason)) {
                reason = String.format(CommonConstant.APPLY_REASON_TEMPLATE,
                        tokenUserDTO.getNickname(),
                        qtGroup.getName(),
                        qtGroup.getGroupId());
            } else {
                reason += "，群组名称为：" + qtGroup.getName() + "，群组ID为：" + qtGroup.getGroupId();
            }

            // 插入申请记录
            // 群组申请，contactId设置为群主ID（简化处理，后续可扩展为通知所有管理员）
            QtContactRequest contactRequest = new QtContactRequest();
            contactRequest.setFromUid(fromUid);
            contactRequest.setToId(groupId);
            contactRequest.setToType(ContactType.GROUP.getCode());
            contactRequest.setContactId(qtGroup.getOwnerUid());
            contactRequest.setReason(reason);
            contactRequest.setStatus(ApplyStatus.PENDING.getCode());
            contactRequest.setCreateTime(new Date());

            qtContactRequestMapper.insert(contactRequest);

            // TODO 发送消息
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setContactId(qtGroup.getOwnerUid());
            messageSendDto.setMessageContent(reason);
            messageSendDto.setMessageType(MessageTypeEnum.ADD_GROUP.getType());
            messageTopicHandler.sendMessage(messageSendDto);
        }

        if (joinType.equals(CommonConstant.THREE)) { // 3:拒绝任何人加入
            throw new QTWebException("该群组拒绝任何人加入");
        }

        // TODO 直接加入实现
    }

    @Override
    public R<List<LoadPendingResponseVO>> loadPendingRequests(String token, String type, LoadPendingRequestsVO loadPendingRequestsVO) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(type, token);

        String uid = tokenUserDTO.getUid();
        Integer receivingType = loadPendingRequestsVO.getReceivingType();
        ContactType contactType = ContactType.getByCode(receivingType);
        ApplyStatus applyStatus = ApplyStatus.PENDING;

        List<LoadPendingBo> loadPendingBo = qtContactRequestMapper.selectPending(uid, contactType.getCode(), applyStatus.getCode());

        if (CollectionUtil.isEmpty(loadPendingBo)) {
            return R.ok(new ArrayList<>());
        }

        List<LoadPendingResponseVO> loadPendingResponseVO = loadPendingBo.stream()
                .map(loadPending -> {
                    LoadPendingResponseVO vo = new LoadPendingResponseVO();
                    vo.setFromUid(loadPending.getFromUid());
                    vo.setNickName(loadPending.getNickName());
                    vo.setAvatar(loadPending.getAvatar());
                    vo.setReason(loadPending.getReason());
                    return vo;
                })
                .collect(Collectors.toList());

        return R.ok(loadPendingResponseVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R handleFormApply(String token, String type, HandleFormApplyVO handleFormApplyVO) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(type, token);

        String uid = tokenUserDTO.getUid();
        String fromUid = handleFormApplyVO.getFromUid();
        String toId = handleFormApplyVO.getToId();
        Integer receivingType = handleFormApplyVO.getReceivingType();

        ContactType contactType = ContactType.getByCode(receivingType);
        ApplyStatus applyStatus = ApplyStatus.PENDING;

        LambdaQueryWrapper<QtContactRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QtContactRequest::getFromUid, fromUid);
        queryWrapper.eq(QtContactRequest::getToId, toId);
        queryWrapper.eq(QtContactRequest::getContactId, uid);
        queryWrapper.eq(QtContactRequest::getToType, contactType.getCode());
        queryWrapper.eq(QtContactRequest::getStatus, applyStatus.getCode());
        QtContactRequest contactRequest = qtContactRequestMapper.selectOne(queryWrapper);

        if (ObjectUtil.isNull(contactRequest)) {
            throw new QTWebException(ResponseCode.TIMEOUT_ERROR.getMessage());
        }

        ApplyStatus handleStatus = ApplyStatus.getByCode(handleFormApplyVO.getHandleResult());

        switch (handleStatus) {
            case AGREE:
                // 添加好友
                if (contactType.equals(ContactType.USER)) {
                    addFriend(uid, fromUid, CommonConstant.ONE);
                }
                if (contactType.equals(ContactType.GROUP)) {
                    addGroupMember(uid, fromUid, toId, CommonConstant.TWO);
                }
                break;
            case REJECT:
                if (NumberUtil.equals(contactRequest.getStatus(), ApplyStatus.PENDING.getCode())) {
                    contactRequest.setStatus(ApplyStatus.REJECT.getCode());
                    contactRequest.setHandleTime(new Date());
                    qtContactRequestMapper.updateById(contactRequest);
                    // TODO 发送拒绝通知
                }else {
                    throw new QTWebException(ResponseCode.TIMEOUT_ERROR.getMessage());
                }
                break;
            case IGNORE:
                if (NumberUtil.equals(contactRequest.getStatus(), ApplyStatus.PENDING.getCode())) {
                    contactRequest.setStatus(ApplyStatus.IGNORE.getCode());
                    contactRequest.setHandleTime(new Date());
                    qtContactRequestMapper.updateById(contactRequest);
                }else {
                    throw new QTWebException(ResponseCode.TIMEOUT_ERROR.getMessage());
                }
                break;
            default:
                throw new QTWebException(ResponseCode.PARAM_ERROR.getMessage());
        }

        return R.ok(ResponseCode.SUCCESS);
    }

    // 添加联系人
    private void addFriend(String ownUid, String friendUid, Integer addType) {
        QtFriend qtFriend = qtFriendMapper.selectOne(new LambdaQueryWrapper<QtFriend>()
                .eq(QtFriend::getUserUid, ownUid)
                .eq(QtFriend::getFriendUid, friendUid));

        if (ObjectUtil.isNotNull(qtFriend) && qtFriend.getStatus().equals(FriendStatus.NORMAL.getCode())) {
            throw new QTWebException("该用户已经是你的好友");
        }
        // 存在则更新
        if (ObjectUtil.isNotNull(qtFriend)) {
            qtFriend.setStatus(FriendStatus.NORMAL.getCode());
            qtFriend.setSource(addType);
            qtFriend.setCreateTime(new Date());
            qtFriendMapper.updateById(qtFriend);
        }

        // 将好友添加为自己的联系人
        List<QtFriend> qtFriendsList = new ArrayList<>();
        QtFriend qtOwn = new QtFriend();
        qtOwn.setUserUid(ownUid);
        qtOwn.setFriendUid(friendUid);
        qtOwn.setSource(addType);
        qtOwn.setStatus(FriendStatus.NORMAL.getCode());
        qtOwn.setCreateTime(new Date());
        qtFriendsList.add(qtOwn);

        // 将自己添加为好友的联系人
        QtFriend QtOther = new QtFriend();
        QtOther.setUserUid(friendUid);
        QtOther.setFriendUid(ownUid);
        QtOther.setSource(addType);
        QtOther.setStatus(FriendStatus.NORMAL.getCode());
        QtOther.setCreateTime(new Date());
        qtFriendsList.add(QtOther);

        qtFriendMapper.insert(qtFriendsList);

        // 发送添加好友成功通知
        if (StrUtil.isAllNotBlank(ownUid, friendUid)) {
            redisComponent.addContactUser(ownUid, friendUid);
            redisComponent.addContactUser(friendUid, ownUid);
        } else {
            log.error("用户:{} 添加联系人:{} 失败", ownUid, friendUid);
            throw new QTWebException(ResponseCode.PARAM_ERROR.getMessage());
        }

        // 初始化会话信息
        SysUser owner = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUid, ownUid));
        SysUser friend = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUid, friendUid));
        String session = ToolUtils.getUserChatSession(ownUid, friendUid);

        List<ChatSessionUser> chatSessionUsers = new ArrayList<>();

        ChatSessionUser ownChatSessionUser = new ChatSessionUser();
        ownChatSessionUser.setSessionId(session);
        ownChatSessionUser.setUid(ownUid);
        ownChatSessionUser.setContactId(friendUid);
        ownChatSessionUser.setContactName(friend.getNickName());
        ownChatSessionUser.setLastMessage(MessageTypeEnum.ADD_FRIEND.getDesc());
        ownChatSessionUser.setLastReceiveTime(new Date().getTime());
        chatSessionUsers.add(ownChatSessionUser);

        ChatSessionUser friendChatSessionUser = new ChatSessionUser();
        friendChatSessionUser.setSessionId(session);
        friendChatSessionUser.setUid(friendUid);
        friendChatSessionUser.setContactId(ownUid);
        friendChatSessionUser.setContactName(owner.getNickName());
        friendChatSessionUser.setLastMessage(MessageTypeEnum.ADD_FRIEND.getDesc());
        friendChatSessionUser.setLastReceiveTime(new Date().getTime());
        chatSessionUsers.add(friendChatSessionUser);

        chatSessionUserMapper.insertOrUpdate(chatSessionUsers);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(session);
        chatMessage.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
        chatMessage.setMessageContent(MessageTypeEnum.ADD_FRIEND.getDesc());
        chatMessage.setSendUserId(ownUid);
        chatMessage.setSendUserNickname(owner.getNickName());
        chatMessage.setSendTime(new Date().getTime());
        chatMessage.setContactId(friendUid);
        chatMessage.setContactType(ContactType.USER.getCode());
        chatMessage.setStatus(CommonConstant.ONE);
        chatMessageMapper.insert(chatMessage);

        MessageSendDto messageSendDto = new MessageSendDto<>();
        BeanUtil.copyProperties(chatMessage, messageSendDto);
        messageTopicHandler.sendMessage(messageSendDto);

        // 发送给申请人
        messageSendDto.setContactId(ownUid);
        messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND_SELF.getType());
        messageSendDto.setExtendData(owner);
        messageTopicHandler.sendMessage(messageSendDto);
    }

    // 添加群成员
    private void addGroupMember(String ownUid, String fromId, String groupId, Integer joinType) {
        QtGroup group = qtGroupMapper.selectOne(new LambdaQueryWrapper<QtGroup>()
                .eq(QtGroup::getGroupId, groupId)
                .eq(QtGroup::getStatus, GroupStatus.NORMAL.getCode()));
        if (ObjectUtil.isNull(group)) {
            throw new QTWebException(ResponseCode.GROUP_NOT_EXISTS.getMessage());
        }

        QtGroupMember qtGroupMember = qtGroupMemberMapper.selectOne(new LambdaQueryWrapper<QtGroupMember>()
                .eq(QtGroupMember::getUserUid, fromId)
                .eq(QtGroupMember::getGroupId, groupId));

        if (ObjectUtil.isNotNull(qtGroupMember) && qtGroupMember.getIsQuit().equals(CommonConstant.ZERO)) {
            throw new QTWebException("该用户已经是群成员");
        }
        if (ObjectUtil.isNotNull(qtGroupMember) && qtGroupMember.getIsQuit().equals(CommonConstant.ONE)) {
            qtGroupMember.setIsQuit(CommonConstant.ZERO);
            qtGroupMember.setJoinTime(new Date());
            qtGroupMember.setJoinType(joinType);
            qtGroupMember.setLeaveTime(null);
            qtGroupMemberMapper.updateById(qtGroupMember);

            qtGroupMapper.update(new LambdaUpdateWrapper<QtGroup>()
                .eq(QtGroup::getGroupId, groupId)
                .set(QtGroup::getCurrentCount, group.getCurrentCount() + 1));
        }

        SysUser owner = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUid, fromId)
                .eq(SysUser::getStatus, StatusEnum.NORMAL.getCode()));

        QtGroupMember member = QtGroupMember.builder()
                .userUid(fromId)
                .groupId(groupId)
                .role(GroupRole.NORMAL.getCode())
                .alias(owner.getNickName())
                .isTop(CommonConstant.ZERO)
                .isDisturb(CommonConstant.ZERO)
                .joinType(joinType)
                .isQuit(CommonConstant.ZERO)
                .joinTime(new Date())
                .build();
        qtGroupMemberMapper.insert(member);

        qtGroupMapper.update(new LambdaUpdateWrapper<QtGroup>()
                .eq(QtGroup::getGroupId, groupId)
                .set(QtGroup::getCurrentCount, group.getCurrentCount() + 1));

        if (StrUtil.isAllNotBlank(fromId, groupId)) {
            redisComponent.addContactGroup(fromId, groupId);
        } else {
            log.error("用户:{} 加入群:{} 失败", fromId, groupId);
            throw new QTWebException(ResponseCode.PARAM_ERROR.getMessage());
        }

        SysUser qtOwn = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUid, fromId));
        QtGroup qtGroup = qtGroupMapper.selectOne(new LambdaQueryWrapper<QtGroup>()
                .eq(QtGroup::getGroupId, groupId));
        String session = ToolUtils.getGroupChatSession(groupId);

        ChatSessionUser ownChatSessionUser = new ChatSessionUser();
        ownChatSessionUser.setSessionId(session);
        ownChatSessionUser.setUid(qtOwn.getUid());
        ownChatSessionUser.setContactId(qtGroup.getGroupId());
        ownChatSessionUser.setContactName(qtGroup.getName());
        ownChatSessionUser.setLastMessage(String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(), qtOwn.getNickName()));
        ownChatSessionUser.setLastReceiveTime(new Date().getTime());

        chatSessionUserMapper.insertOrUpdate(ownChatSessionUser);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(session);
        chatMessage.setMessageType(MessageTypeEnum.ADD_GROUP.getType());
        chatMessage.setMessageContent(String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(), qtOwn.getNickName()));
        chatMessage.setSendUserId(qtOwn.getUid());
        chatMessage.setSendUserNickname(qtOwn.getNickName());
        chatMessage.setSendTime(new Date().getTime());
        chatMessage.setContactId(qtGroup.getGroupId());
        chatMessage.setContactType(ContactType.GROUP.getCode());
        chatMessage.setStatus(CommonConstant.ONE);
        chatMessageMapper.insert(chatMessage);

        MessageSendDto messageSendDto = new MessageSendDto();
        BeanUtil.copyProperties(chatMessage, messageSendDto);
        messageSendDto.setMemberCount(qtGroup.getCurrentCount());
        messageSendDto.setContactNickName(qtGroup.getName());

        messageTopicHandler.sendMessage(messageSendDto);
    }
}