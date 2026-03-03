package course.QTalk.pojo.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "群组成员信息")
public class GroupMemberInfoBO {
    @Schema(description = "群内成员的uid")
    private String uid;

    @Schema(description = "群内成员的角色昵称")
    private String alias;

    @Schema(description = "群内成员的角色(1:")
    private String role;

    @Schema(description = "群内成员头像")
    private String avatar;
}
