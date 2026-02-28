package course.QTalk.minio.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import course.QTalk.minio.properties.MinIOConfigProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO configuration class
 */
@Slf4j
@Configuration
@ComponentScan("course.QTalk.minio")
@EnableConfigurationProperties(MinIOConfigProperties.class)
public class MinIOConfig {

    private final MinIOConfigProperties minIOConfigProperties;

    public MinIOConfig(MinIOConfigProperties minIOConfigProperties) {
        this.minIOConfigProperties = minIOConfigProperties;
    }

    @Bean
    public MinioClient minioClient() {
        log.info("带端点初始化 MinIO 客户端: {}", minIOConfigProperties.getEndpoint());
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minIOConfigProperties.getEndpoint())
                    .credentials(minIOConfigProperties.getAccessKey(), minIOConfigProperties.getSecretKey())
                    .build();

            minioClient.setTimeout(
                    minIOConfigProperties.getConnectTimeout(),
                    minIOConfigProperties.getWriteTimeout(),
                    minIOConfigProperties.getReadTimeout()
            );

            return minioClient;
        } catch (Exception e) {
            log.error("未能初始化 MinIO 客户端", e);
            throw new RuntimeException("未能初始化 MinIO 客户端", e);
        }
    }
}
