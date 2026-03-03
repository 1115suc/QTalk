package course.QTalk.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "搜索用户请求参数")
public class UserSearchVO {
    @Schema(description = "用户ID")
    private String uid;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "用户邮箱")
    private String email;
}
