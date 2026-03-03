package course.QTalk.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "更新用户信息请求参数")
public class UpdateUserInfoVO {
    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户简介")
    private String description;

    @Schema(description = "生日")
    private Date birthday;

    @Schema(description = "是否允许添加好友(0.同意后加好友 1.直接加好友 2.不允许加好友)")
    @Min(value = 0, message = "是否允许添加好友参数错误")
    @Max(value = 2, message = "是否允许添加好友参数错误")
    private Integer addFriends;

    @Schema(description = "性别(0.未知 1.男 2.女)")
    @Min(value = 0, message = "性别参数错误")
    @Max(value = 2, message = "性别参数错误")
    private Integer sex;

    @Schema(description = "地区名称")
    private String areaName;
}