package course.QTalk.pojo.vo.response;

import course.QTalk.pojo.bo.GroupMemberInfoBO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
@Schema(description = "群详情信息")
public class GroupDetailInfoVO {
    @Schema(description = "群组ID")
    private String groupId;

    @Schema(description = "群组名称")
    private String name;

    @Schema(description = "群组头像URL")
    private String avatar;

    @Schema(description = "群主UID")
    private String ownerUid;

    @Schema(description = "群组公告")
    private String notice;

    @Schema(description = "当前成员数")
    private Integer currentCount;

    @Schema(description = "是否允许普通成员邀请(0:否, 1:是)")
    private Integer allowInvite;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "群成员")
    private List<GroupMemberInfoBO> members;
}
