package course.QTalk.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "处理表单申请")
public class HandleFormApplyVO {
    @Schema(description = "发送方UID")
    private String fromUid;

    @Schema(description = "申请好友ID或群ID")
    private String toId;

    @Schema(description = "接收类型(1:用户, 2:群组)")
    @NotNull(message = "接收参数类型不能为空")
    @Min(value = 1, message = "接收参数错误")
    @Max(value = 2, message = "接收参数错误")
    private Integer receivingType;

    @Schema(description = "处理结果(1:同意, 2:拒绝, 3:忽略)")
    @NotNull(message = "处理结果不能为空")
    @Min(value = 1, message = "处理结果错误")
    @Max(value = 3, message = "处理结果错误")
    private Integer handleResult;
}
