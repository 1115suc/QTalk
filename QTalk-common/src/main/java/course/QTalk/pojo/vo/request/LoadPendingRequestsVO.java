package course.QTalk.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "加载待处理的请求")
public class LoadPendingRequestsVO {
    @Schema(description = "接收类型(1:用户, 2:群组)")
    @NotNull(message = "接收参数类型不能为空")
    @Min(value = 1, message = "接收参数错误")
    @Max(value = 2, message = "接收参数错误")
    private Integer receivingType;
}
