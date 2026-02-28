package course.QTalk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAspectJAutoProxy
@EnableTransactionManagement
@MapperScan("course.QTalk.mapper")
@SpringBootApplication(scanBasePackages = {"course.QTalk"})
public class QTalkBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(QTalkBackendApplication.class, args);
    }
}
