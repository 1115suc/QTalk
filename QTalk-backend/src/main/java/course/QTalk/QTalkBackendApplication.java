package course.QTalk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("course.QTalk.mapper")
@SpringBootApplication
public class QTalkBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(QTalkBackendApplication.class, args);
    }
}
