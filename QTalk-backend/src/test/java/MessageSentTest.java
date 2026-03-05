import course.QTalk.QTalkBackendApplication;
import course.QTalk.handler.MessageTopicHandler;
import course.QTalk.pojo.dto.MessageSendDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = QTalkBackendApplication.class)
public class MessageSentTest {
    @Autowired
    private MessageTopicHandler messageTopicHandler;

    @Test
    public void test() {
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageContent("测试消息");
        messageSendDto.setContactId("U127766356");
        messageSendDto.setContactNickName("测试");
        messageTopicHandler.sendMessage(messageSendDto);
    }
}
