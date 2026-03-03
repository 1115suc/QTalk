package course.QTalk.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户搜索结果信息")
public class UserSearchInfoVO {
    @Schema(description = "用户ID")
    private String uid;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "性别(0.未知 1.男 2.女)")
    private Integer sex;

    @Schema(description = "地区名称")
    private String areaName;
    
    @Schema(description = "是否允许添加好友(0.同意后加好友 1.直接加好友 2.不允许加好友)")
    private Integer addFriends;
}
