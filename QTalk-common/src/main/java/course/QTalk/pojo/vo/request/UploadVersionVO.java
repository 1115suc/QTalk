package course.QTalk.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "上传版本信息请求参数")
public class UploadVersionVO {
    @Schema(description = "版本号 (如：1.0.0)")
    @NotBlank(message = "版本号不能为空")
    private String version;

    @Schema(description = "平台类型 (1:Web 端 2:Android 3: iOS)")
    @NotNull(message = "平台类型不能为空")
    @Min(value = 1, message = "平台类型参数错误")
    @Max(value = 3, message = "平台类型参数错误")
    private Integer platform;

    @Schema(description = "更新描述 (支持 HTML/Markdown)")
    private String updateDesc;

    @Schema(description = "文件类型 (1:完整包 2:增量包 3:外部分发链接)")
    @NotNull(message = "文件类型不能为空")
    @Min(value = 1, message = "文件类型参数错误")
    @Max(value = 3, message = "文件类型参数错误")
    private Integer fileType;

    @Schema(description = "状态 (0:未发布, 1:全网发布, 2:灰度发布)")
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态参数错误")
    @Max(value = 2, message = "状态参数错误")
    private Integer status;
}
