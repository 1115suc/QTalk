package course.QTalk.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "申请加好友/群,对象类")
public class ApplyJoinContactVO {
    @Schema(description = "对方UID或者群ID")
    @NotBlank(message = "对方UID或者群ID不能为空")
    private String applyId;

    @Schema(description = "申请类型：1.加好友 2.加群")
    @NotNull(message = "申请类型不能为空")
    @Min(value = 1, message = "申请参数错误")
    @Max(value = 2, message = "申请参数错误")
    private Integer applyType;

    @Schema(description = "申请理由")
    private String applyReason;
}
