package course.QTalk.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "搜索用户请求参数")
public class UserSearchVO {
    @Schema(description = "搜索关键字(用户ID/昵称/邮箱)")
    @NotBlank(message = "搜索关键字不能为空")
    private String key;
}
