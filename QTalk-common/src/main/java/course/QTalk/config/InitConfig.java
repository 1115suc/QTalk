package course.QTalk.config;

import course.QTalk.websocket.netty.NettyWebSocketStarter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InitConfig implements ApplicationRunner {

    private final DataSource dataSource;
    private final NettyWebSocketStarter nettyWebSocketStarter;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            log.info("数据源初始化中...");
            dataSource.getConnection();
            log.info("Netty WebSocket 服务启动中...");
            new Thread(nettyWebSocketStarter).start();
        } catch (SQLException e) {

            log.error("数据库配置错误，请检查数据库配置");
        } catch (Exception e) {
            log.error("Netty Websocket 启动失败...");
        }
    }
}
