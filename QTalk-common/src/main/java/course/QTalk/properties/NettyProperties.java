package course.QTalk.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "netty")
@ConditionalOnProperty(prefix = "netty",value = "ip")
public class NettyProperties {
    private String ip;
    private Integer port;
    private String path;
}
