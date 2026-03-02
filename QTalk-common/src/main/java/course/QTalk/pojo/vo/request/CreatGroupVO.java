package course.QTalk.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "创建群聊请求参数")
public class CreatGroupVO {
    @Schema(description = "群聊名称")
    @NotBlank(message = "群聊名称不能为空")
    private String groupName;

    @Schema(description = "群聊公告")
    private String notice;

    @Schema(description = "是否允许普通成员邀请(0:否, 1:是)，默认1")
    @Min(value = 0, message = "是否允许普通成员邀请参数错误")
    @Max(value = 1, message = "是否允许普通成员邀请参数错误")
    private Integer allowInvite;

    @Schema(description = "入群方式(0:同意后加入, 1:直接加入, 2:邀请加入, 3:拒绝任何人加入)，默认0")
    @Min(value = 0, message = "入群方式参数错误")
    @Max(value = 3, message = "入群方式参数错误")
    private Integer joinType;

    @Schema(description = "群聊头像", type = "file", format = "binary")
    private MultipartFile avatar;
}
