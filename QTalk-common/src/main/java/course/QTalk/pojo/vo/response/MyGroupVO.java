package course.QTalk.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "我的群组信息")
public class MyGroupVO {
    @Schema(description = "群组ID")
    private String groupId;

    @Schema(description = "群组名称")
    private String groupName;

    @Schema(description = "群组头像")
    private String groupAvatar;

    // 我的角色 1:普通成员 2:管理员 3:群主
    @Schema(description = "我的角色(1:普通成员 2:管理员 3:群主)")
    private Integer role;
}
