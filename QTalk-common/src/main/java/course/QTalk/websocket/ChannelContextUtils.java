package course.QTalk.websocket;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.UserInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import course.QTalk.constant.CommonConstant;
import course.QTalk.mapper.ChatMessageMapper;
import course.QTalk.mapper.ChatSessionUserMapper;
import course.QTalk.mapper.QtContactRequestMapper;
import course.QTalk.mapper.SysUserMapper;
import course.QTalk.pojo.dto.MessageSendDto;
import course.QTalk.pojo.dto.WsInitDataDTO;
import course.QTalk.pojo.enums.ApplyStatus;
import course.QTalk.pojo.enums.ContactType;
import course.QTalk.pojo.enums.MessageTypeEnum;
import course.QTalk.pojo.po.ChatMessage;
import course.QTalk.pojo.po.ChatSessionUser;
import course.QTalk.pojo.po.QtContactRequest;
import course.QTalk.pojo.po.SysUser;
import course.QTalk.util.RedisComponent;
import course.QTalk.util.RedisUtil;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelContextUtils {

    private static final ConcurrentHashMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();

    private final RedisUtil redisUtil;
    private final RedisComponent redisComponent;
    private final SysUserMapper sysUserMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionUserMapper chatSessionUserMapper;
    private final QtContactRequestMapper qtContactRequestMapper;

    /**
     * 将用户 UID 与 Netty Channel 进行绑定，建立用户会话上下文
     * <p>
     * 该方法通过将用户 ID 存储到 Channel 的属性中，实现用户与连接的关联。
     * 后续可以通过 Channel 快速获取对应的用户信息，用于消息推送、权限验证等场景。
     *
     * @param uid     用户的唯一标识符，用于标识当前连接所属的用户
     * @param channel Netty 通信渠道对象，代表与客户端的网络连接
     */
    public void addContext(String uid, Channel channel) {
        // 获取 Channel 的唯一标识符（格式：[id: 0x12345678]）
        String channelId = channel.id().toString();
        log.info("用户 id：{}", uid);
        log.info("channelId：{}", channelId);

        AttributeKey attributeKey = null;
        // 检查该 channelId 是否已存在对应的 AttributeKey
        if (!AttributeKey.exists(channelId)) {
            // 不存在则创建新的 AttributeKey
            attributeKey = AttributeKey.newInstance(channelId);
        } else {
            // 已存在则复用现有的 Key，避免重复创建
            attributeKey = AttributeKey.valueOf(channelId);
        }
        // 将用户 UID 绑定到 Channel 的属性中，实现用户与连接的关联
        Attribute attr = channel.attr(attributeKey);
        attr.set(uid);

        // 获取用户所属的群组列表，并将当前 Channel 加入到对应的群组频道组中
        List<Object> contactGroupList = redisComponent.getContactGroupList(uid);
        if (ObjectUtil.isNotNull(contactGroupList)) {
            // 遍历用户的群组列表，将当前连接添加到每个群组的 ChannelGroup 中
            for (Object groupId : contactGroupList) {
                String tempGroupId = Convert.toStr(groupId);
                addChannelToGroup(tempGroupId, channel);
            }
        }

        // 更新用户最后的连接时间
        if (StrUtil.isNotBlank(uid)) {
            // 建立用户 UID 与 Channel 的映射关系，用于后续的单聊消息精准推送
            USER_CONTEXT_MAP.put(uid, channel);

            // 更新用户的最后登录时间（记录离线时间）
            UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("uid", uid);
            SysUser sysUser = new SysUser();
            sysUser.setLastLoginTime(new Date());
            sysUserMapper.update(sysUser, updateWrapper);
        }

        // 给用户发送消息
        LambdaQueryWrapper<SysUser> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(SysUser::getUid, uid);
        SysUser sysUser = sysUserMapper.selectOne(userLambdaQueryWrapper);
        Long lastLoginTime = sysUser.getLastLoginTime().getTime();
        // 如果用户最后登录时间为空，则设置为三天前
        if (ObjectUtil.isEmpty(lastLoginTime)) {
            lastLoginTime = System.currentTimeMillis() - CommonConstant.L_THREE_DAY;
        }


        /*
         *   1、获取用户最后登录时间，如果小于3天，则查询用户的所有会话信息
         * */
        LambdaQueryWrapper<ChatSessionUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSessionUser::getUid, uid);
        queryWrapper.orderByDesc(ChatSessionUser::getLastReceiveTime);
        List<ChatSessionUser> chatSessionUserList = chatSessionUserMapper.selectList(queryWrapper);

        WsInitDataDTO wsInitData = new WsInitDataDTO();
        wsInitData.setChatSessionUserList(chatSessionUserList);

        /*
         *   2、查询聊天消息
         * */
        LambdaQueryWrapper<ChatMessage> messageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<ChatMessage> chatMessageList = new ArrayList<>();

        // 查询群聊消息
        if (CollectionUtil.isNotEmpty(contactGroupList)) {
            messageLambdaQueryWrapper.in(ChatMessage::getContactId, contactGroupList);
            messageLambdaQueryWrapper.ge(ChatMessage::getSendTime, lastLoginTime);
            chatMessageList.addAll(chatMessageMapper.selectList(messageLambdaQueryWrapper));
        }

        // 查询好友消息
        List<Object> contactUserList = redisComponent.getContactUserList(uid);
        if (CollectionUtil.isNotEmpty(contactUserList)) {
            messageLambdaQueryWrapper.in(ChatMessage::getContactId, contactUserList);
            messageLambdaQueryWrapper.ge(ChatMessage::getSendTime, lastLoginTime);
            chatMessageList.addAll(chatMessageMapper.selectList(messageLambdaQueryWrapper));
        }

        // 如果有消息，则设置给wsInitData相应给前端
        if (CollectionUtil.isNotEmpty(chatMessageList)) {
            wsInitData.setChatMessageList(chatMessageList);
        }

        /*
         *   3、查询好友申请
         * */
        LambdaQueryWrapper<QtContactRequest> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(QtContactRequest::getContactId, uid);
        countWrapper.eq(QtContactRequest::getStatus, ApplyStatus.PENDING.getCode());
        countWrapper.gt(QtContactRequest::getCreateTime, new Date(lastLoginTime - CommonConstant.L_SIX_DAY));
        Long count = qtContactRequestMapper.selectCount(countWrapper);
        wsInitData.setApplyCount(count);

        // 发送消息

        //发送消息
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDto.setContactId(uid);
        messageSendDto.setExtendData(wsInitData);

        sendMsg(messageSendDto, uid);
    }

    // 发送消息
    public void sendMsg(MessageSendDto messageSendDto, String receiveId) {
        Channel userChannel = USER_CONTEXT_MAP.get(receiveId);
        if (userChannel == null) {
            return;
        }

        // 相对于 客户端 而言，联系人就是发送人，所以这里转换一下再发送
        if (MessageTypeEnum.ADD_FRIEND_SELF.getType().equals(messageSendDto.getMessageType())) {
            SysUser sysUser = (SysUser) messageSendDto.getExtendData();
            messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
            messageSendDto.setContactId(sysUser.getUid());
            messageSendDto.setContactNickName(sysUser.getNickName());
            messageSendDto.setExtendData(null);
        } else {
            messageSendDto.setContactId(messageSendDto.getSendUserId());
            messageSendDto.setContactNickName(messageSendDto.getSendUserNickName());
        }
        userChannel.writeAndFlush(
                new TextWebSocketFrame(JSONUtil.toJsonPrettyStr(messageSendDto))
        );
    }

    private void addChannelToGroup(String groupId, Channel channel) {
        // 从缓存中获取群组对应的 ChannelGroup
        ChannelGroup group = GROUP_CONTEXT_MAP.get(groupId);

        // 如果群组的 ChannelGroup 不存在，则创建新的 ChannelGroup 并缓存
        if (ObjectUtil.isNull(group)) {
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId, group);
        }

        // 校验 Channel 是否有效，无效则直接返回
        if (ObjectUtil.isNull(channel)) {
            return;
        }

        // 将当前连接添加到群组的 ChannelGroup 中
        group.add(channel);
    }

    public void sendMessage(MessageSendDto messageSendDto) {
        ContactType contactTypeEnum = ContactType.getByPrefix(messageSendDto.getContactId());
        switch (contactTypeEnum) {
            case USER:
                sendToUser(messageSendDto);
                break;
            case GROUP:
                sendToGroup(messageSendDto);
                break;
        }
    }

    /**
     * 发送给用户
     */
    private void sendToUser(MessageSendDto messageSendDto) {
        String contactId = messageSendDto.getContactId();
        if (StrUtil.isBlank(contactId)) {
            return;
        }
        sendMsg(messageSendDto, contactId);
        // 强制下线
        if (MessageTypeEnum.FORCE_OFF_LINE.getType().equals(messageSendDto.getMessageType())) {
            closeContext(contactId);
        }
    }

    // 关闭用户会话上下文
    public void closeContext(String userId) {
        if (StrUtil.isBlank(userId)) {
            return;
        }

        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (channel == null) {
            return;
        }
        channel.close();
    }

    /**
     * 发送给群组
     */
    private void sendToGroup(MessageSendDto messageSendDto) {
        if (StrUtil.isBlank(messageSendDto.getContactId())) {
            return;
        }

        ChannelGroup channelGroup = GROUP_CONTEXT_MAP.get(messageSendDto.getContactId());
        if (channelGroup == null) {
            return;
        }
        channelGroup.writeAndFlush(
                new TextWebSocketFrame(JSONUtil.toJsonPrettyStr(messageSendDto))
        );
    }

    /**
     * 移除用户会话上下文，处理用户断开连接时的清理工作
     * <p>
     * 该方法从本地缓存中移除用户 UID 与 Channel 的映射关系，
     * 并将用户从所有群组的 ChannelGroup 中移除，最后更新用户的离线时间。
     *
     * @param channel Netty 通信渠道对象，代表与客户端断开的网络连接
     */
    public void removeContext(Channel channel) {
        // 从 Channel 属性中获取之前绑定的用户 UID
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String uid = attribute.get();

        // 【关键】只有 UID 有效时才执行清理操作
        if (StrUtil.isNotBlank(uid)) {
            log.info("用户断开连接：uid={}", uid);

            // 1. 从用户上下文缓存中移除
            USER_CONTEXT_MAP.remove(uid);

            // 2. 从所有群组频道组中移除该用户的连接
            for (ChannelGroup group : GROUP_CONTEXT_MAP.values()) {
                group.remove(channel);
            }

            // 3. 更新用户的最后登录时间（记录离线时间）
            try {
                UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("uid", uid);
                SysUser sysUser = new SysUser();
                sysUser.setLastLoginTime(new Date(System.currentTimeMillis()));
                sysUserMapper.update(sysUser, updateWrapper);
                log.info("用户 {} 离线时间已更新", uid);
            } catch (Exception e) {
                log.error("更新用户 {} 离线时间失败：{}", uid, e.getMessage());
            }
        } else {
            log.warn("无法获取用户 UID，跳过清理操作，channelId={}", channel.id());
        }
    }
}