package course.QTalk.controller;

import cn.hutool.core.convert.Convert;
import course.QTalk.annotation.VerificationInterceptor;
import course.QTalk.pojo.vo.request.UpdateUserInfoVO;
import course.QTalk.pojo.vo.response.R;
import course.QTalk.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户信息管理", description = "用户信息相关接口，包括更新用户信息等功能")
public class UserInfoController {

    private final SysUserService sysUserService;

    @Operation(
            summary = "更新用户信息",
            description = "更新用户的基本信息，包括昵称、头像、简介、生日、交友设置、性别、地区等。",
            method = "PUT"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
    })
    @VerificationInterceptor(checkLogin = true)
    @PutMapping
    public R updateUserInfo(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "登录方式不能为空") @RequestHeader("LoginType") String loginType,
            @RequestBody UpdateUserInfoVO updateUserInfoVO
    ) {
        Integer type = Convert.toInt(loginType);
        return sysUserService.updateUserInfo(token, type, updateUserInfoVO);
    }

    // TODO 实名认证

    // TODO 获取用户信息
}
