package course.QTalk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import course.QTalk.properties.SwaggerConfigProperties;

@Configuration
@ConditionalOnBean(value = SwaggerConfigProperties.class)
public class Knife4jConfiguration {

    @Autowired
    private SwaggerConfigProperties swaggerConfigProperties;

    /**
     * 全局 OpenAPI 信息（替代原来的 ApiInfoBuilder）
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(swaggerConfigProperties.getTitle())
                        .description(swaggerConfigProperties.getDescription())
                        .version(swaggerConfigProperties.getVersion())
                        .contact(new Contact()
                                .name(swaggerConfigProperties.getContactName())
                                .url(swaggerConfigProperties.getContactUrl())
                                .email(swaggerConfigProperties.getContactEmail())
                        )
                );
    }
    /**
     * 分组扫描（替代原来的 Docket + basePackage）
     */
    @Bean
    public GroupedOpenApi qtalkApi() {
        return GroupedOpenApi.builder()
                .group("QTalkApi")
                .packagesToScan(swaggerConfigProperties.getPackagePath())
                .build();
    }
}