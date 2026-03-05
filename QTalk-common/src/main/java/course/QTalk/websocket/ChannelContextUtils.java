package course.QTalk.websocket;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import course.QTalk.constant.CommonConstant;
import course.QTalk.mapper.ChatSessionUserMapper;
import course.QTalk.mapper.SysUserMapper;
import course.QTalk.pojo.dto.WsInitDataDTO;
import course.QTalk.pojo.po.ChatSessionUser;
import course.QTalk.pojo.po.SysUser;
import course.QTalk.util.RedisComponent;
import course.QTalk.util.RedisUtil;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
    private final ChatSessionUserMapper chatSessionUserMapper;

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
        Date lastLoginTime = sysUser.getLastLoginTime();
        long lastLoginTimeStamp = lastLoginTime.getTime();
        // 如果用户最后登录时间小于3天，则将最后登录时间设置为3天
        if (ObjectUtil.isNotNull(lastLoginTime) && lastLoginTime.getTime() < System.currentTimeMillis() - CommonConstant.L_THREE_DAY) {
            lastLoginTimeStamp = CommonConstant.L_THREE_DAY;
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