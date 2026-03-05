package course.QTalk.websocket.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatHandler extends ChannelDuplexHandler {

    // 处理用户自定义事件的回调方法。当有特定事件触发时，Netty 会自动调用这个方法。
     @Override
     public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

         if (evt instanceof IdleStateEvent) {
             IdleStateEvent event = (IdleStateEvent) evt;

             switch (event.state()) {
                 // 读空闲：超过指定时间没有收到对方消息
                 case READER_IDLE:
                     Channel channel = ctx.channel();
                     Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));

                     String userId = attribute.get();
                     log.debug("客户端用户{}读超时，可能已断开连接", userId);
                     ctx.close();
                     break;
                 case WRITER_IDLE:
                     // 写空闲：超过指定时间没有发送消息
                     log.debug("服务端写超时，发送心跳包");
                     // 创建并发送 WebSocket Ping 帧（心跳包）
                     // Ping 帧是 WebSocket 协议内置的心跳机制，用于：
                     // 1. 检测客户端连接是否仍然存活
                     // 2. 防止防火墙或路由器因连接长时间无数据而自动断开
                     // 3. 保持 TCP 连接活跃，避免中间网络设备认为连接已失效
                     // 4. 客户端收到 Ping 后会自动回复 Pong 帧（RFC 6455 标准）
                     ctx.writeAndFlush(new PingWebSocketFrame());
                     break;
                 case ALL_IDLE:
                     // 读写都空闲
                     log.debug("读写都空闲");
                     break;
             }
         } else {
             // 调用父类方法，将事件传递给 Pipeline 中的下一个 Handler
             super.userEventTriggered(ctx, evt);
         }
     }
}