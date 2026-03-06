package course.QTalk.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "用户发送文件请求VO")
public class SendFileVO {
    @Schema(description = "消息Id")
    @NotNull(message = "消息Id不能为空")
    private String messageId;

    @Schema(description = "文件")
    @NotNull(message = "文件不能为空")
    private MultipartFile multipartFile;
}
