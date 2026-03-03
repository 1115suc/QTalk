package course.QTalk.controller;


import course.QTalk.annotation.VerificationInterceptor;
import course.QTalk.pojo.vo.request.GroupBasicInfoVO;
import course.QTalk.pojo.vo.request.UserSearchVO;
import course.QTalk.pojo.vo.response.GroupInfoVO;
import course.QTalk.pojo.vo.response.R;
import course.QTalk.pojo.vo.response.UserSearchInfoVO;
import course.QTalk.service.QtGroupService;
import course.QTalk.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
@Tag(name = "联系人/群管理", description = "联系人/群相关接口，包括搜索联系人/群、申请加入群聊，好友申请等功能")
public class UserContactController {

    private final QtGroupService groupService;
    private final SysUserService sysUserService;

    @Operation(
            summary = "搜索用户",
            description = "根据用户ID、邮箱或昵称搜索用户。",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping("/searchUser")
    public R<List<UserSearchInfoVO>> searchUser(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "登录方式不能为空") @RequestHeader("LoginType") String loginType,
            @NotNull(message = "搜索信息不能为空") @RequestBody UserSearchVO userSearchVO) {
        return sysUserService.searchUser(userSearchVO);
    }

    @Operation(
            summary = "搜索群聊",
            description = "根据群组ID查询群组的详细信息，包括群聊名称、公告、成员数量、创建时间等,用于搜索加入群聊。",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping("/queryGroupInfo")
    public R<List<GroupInfoVO>> queryGroupInfo(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "登录方式不能为空") @RequestHeader("LoginType") String loginType,
            @NotNull(message = "群组基础信息不能为空") @RequestBody GroupBasicInfoVO groupBasicInfoVO) {
        return groupService.queryGroupInfo(groupBasicInfoVO);
    }
}
