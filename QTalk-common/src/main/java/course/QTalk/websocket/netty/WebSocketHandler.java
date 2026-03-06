package course.QTalk.websocket.netty;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import course.QTalk.exception.QTWebException;
import course.QTalk.pojo.dto.TokenUserDTO;
import course.QTalk.pojo.enums.ResponseCode;
import course.QTalk.handler.RedisComponent;
import course.QTalk.util.RedisUtil;
import course.QTalk.websocket.ChannelContextUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final RedisUtil redisUtil;
    private final RedisComponent redisComponent;
    private final ChannelContextUtils channelContextUtils;

    /**
     * 处理 WebSocket 文本消息读取事件
     * 当客户端发送文本消息时触发，从 Channel 属性中获取用户 ID 并记录消息日志
     *
     * @param ctx Channel 处理上下文，包含当前连接通道信息和处理器引用
     * @param textWebSocketFrame 接收到的文本 WebSocket 帧对象，封装了客户端发送的文本消息内容
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        // 获取当前连接的 Channel 对象，代表与客户端的通信通道
        Channel channel = ctx.channel();
        // 通过 Channel ID 生成唯一的属性键，获取存储在该 Channel 上的用户 ID 属性
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        // 从属性容器中读取之前存储的用户 ID
        String userId = attribute.get();
        log.debug("用户 id：{} 的消息：{}", userId, textWebSocketFrame.text());
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 处理 WebSocket 握手完成事件，建立用户连接上下文
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            log.info("WebSocket 握手完成");
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            // 从握手完成事件中获取请求头
            HttpHeaders entries = complete.requestHeaders();

            String loginType = entries.get("LoginType");
            String token = entries.get("Authorization");

            if (StrUtil.isBlank(loginType) || StrUtil.isBlank(token)) {
                throw new QTWebException(ResponseCode.HEADER_EMPTY_PARAM.getMessage());
            }

            try {
                // 校验用户信息是否有效，无效则关闭连接（认证失败）
                TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(loginType, token);
                if (ObjectUtil.isNull(tokenUserDTO)) {
                    log.error("WebSocket 认证失败：无效的token或参数");
                    ctx.channel().close();
                    return;
                }
                // 将用户 UID 与当前 Channel 绑定，建立用户会话上下文，用于后续消息推送
                channelContextUtils.addContext(tokenUserDTO.getUid(), ctx.channel());
                log.info("WebSocket 连接认证成功，用户ID：{}", tokenUserDTO.getUid());
            } catch (Exception e) {
                log.error("WebSocket 认证过程中发生异常：{}", e.getMessage());
                ctx.channel().close();
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的连接加入... 远程地址: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("有连接断开...");
        channelContextUtils.removeContext(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("WebSocket 连接异常: ", cause);
        ctx.close();
    }
}
