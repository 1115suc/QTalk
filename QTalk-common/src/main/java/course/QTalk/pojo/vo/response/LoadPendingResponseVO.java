package course.QTalk.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "待处理请求响应")
public class LoadPendingResponseVO {
    @Schema(description = "发送方UID")
    private String fromUid;

    @Schema(description = "发送方名称")
    private String nickName;

    @Schema(description = "发送方头像URL")
    private String avatar;

    @Schema(description = "申请理由")
    private String reason;
}
