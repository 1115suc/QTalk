package course.QTalk.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAspectJAutoProxy
@EnableTransactionManagement
@MapperScan("course.QTalk.mapper")
@SpringBootApplication(scanBasePackages = {"course.QTalk"})
public class QTalkAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(QTalkAdminApplication.class, args);
    }
}
