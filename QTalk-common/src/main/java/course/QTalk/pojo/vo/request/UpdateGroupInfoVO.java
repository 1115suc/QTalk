package course.QTalk.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "更新群组信息请求参数")
public class UpdateGroupInfoVO {
    @Schema(description = "群组ID")
    @NotBlank(message = "群组ID不能为空")
    private String groupId;

    @Schema(description = "群组名称")
    private String name;

    @Schema(description = "群组头像URL")
    private MultipartFile avatar;

    @Schema(description = "群组公告")
    private String notice;

    @Schema(description = "入群方式(0:同意后加入, 1:直接加入, 2:邀请加入, 3:拒绝任何人加入)")
    @NotNull(message = "入群方式不能为空")
    @Min(value = 0, message = "入群方式参数错误")
    @Max(value = 3, message = "入群方式参数错误")
    private Integer joinType;
}
