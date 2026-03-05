package course.QTalk.websocket.netty;

import course.QTalk.properties.NettyProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class NettyWebSocketStarter implements Runnable {

    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static EventLoopGroup workerGroup = new NioEventLoopGroup(2);

    private final WebSocketHandler webSocketHandler;
    private final NettyProperties nettyProperties;

    @Override
    public void run() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            // 添加日志处理器，用于调试
                            pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
                            // 对http协议的支持，使用http协议的解码器
                            pipeline.addLast(new HttpServerCodec());
                            // 聚合解码 heepRequest/HttpContent/LastHttpContent,保证接受到heep的完整性
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            // 心跳 long readerIdleTime 读超时，即测试端一定事件内未接受到被测试段消息，则断开连接
                            pipeline.addLast(new IdleStateHandler(600, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new HeartBeatHandler());
                            /**
                             * 构建 WebSocket 服务器协议配置对象
                             * 配置 WebSocket 握手、帧大小限制、超时等核心参数
                             */
                            WebSocketServerProtocolConfig wsConfig = WebSocketServerProtocolConfig.newBuilder()
                                    // 设置 WebSocket 路径，从配置中读取（如：/websocket）
                                    .websocketPath(nettyProperties.getPath())
                                    // 启用路径前缀匹配，允许 /websocket/* 形式的 URL 都能匹配
                                    .checkStartsWith(true)
                                    // 设置最大帧载荷长度为 65536 字节（64KB），超过此大小的消息将被拒绝
                                    .maxFramePayloadLength(65536)
                                    // 设置握手超时时间为 10000 毫秒（10 秒），超时未完成握手则关闭连接
                                    .handshakeTimeoutMillis(10000L)
                                    // 允许 WebSocket 扩展，支持客户端协商扩展功能（如压缩）
                                    .allowExtensions(true)
                                    .build();
                            // 添加 WebSocket 协议处理器，负责处理 HTTP 到 WebSocket 的升级握手
                            pipeline.addLast(new WebSocketServerProtocolHandler(wsConfig));
                            // 添加自定义业务处理器，处理 WebSocket 消息的读写事件
                            pipeline.addLast(webSocketHandler);
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap
                    .bind(nettyProperties.getPort())
                    .sync();
            log.info("Netty WebSocket 服务初始化完成... 端口:{}", nettyProperties.getPort());
            log.info("Netty WebSocket 请求url: ws://{}:{}/{}", nettyProperties.getIp(), nettyProperties.getPort(), nettyProperties.getPath());
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @PreDestroy
    public void close () {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
