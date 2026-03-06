package course.QTalk.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "发送消息请求VO")
public class SendMessageVO {
    @Schema(description = "联系人ID")
    @NotBlank(message = "联系人ID不能为空")
    private String contactId;

    @Schema(description = "消息内容")
    @NotBlank(message = "消息内容不能为空")
    private String messageContent;

    @Schema(description = "消息类型 2:普通聊天消息 5:文件")
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件类型")
    private Integer fileType;
}
