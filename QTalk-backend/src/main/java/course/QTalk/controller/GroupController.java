package course.QTalk.controller;

import cn.hutool.core.convert.Convert;
import course.QTalk.annotation.Idempotent;
import course.QTalk.annotation.VerificationInterceptor;
import course.QTalk.pojo.vo.request.CreatGroupVO;
import course.QTalk.pojo.vo.response.GroupInfoVO;
import course.QTalk.pojo.vo.response.MyGroupVO;
import course.QTalk.pojo.vo.response.R;
import course.QTalk.service.QtGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
@Tag(name = "群组管理", description = "群组相关接口，包括创建群组、加入群组、退出群组等功能")
public class GroupController {

    private final QtGroupService groupService;

    @Operation(
            summary = "创建群聊",
            description = "创建群聊并上传头像，支持设置群聊名称、公告、入群方式等配置。",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "X-Request-Id", description = "幂等键", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "creatGroupVO", description = "群聊信息表单", required = true)
    })
    @VerificationInterceptor(checkLogin = true)
    @Idempotent(expire = 10, message = "群组创建中，请勿重复提交")
    @PostMapping(consumes = "multipart/form-data")
    public R createGroup(
            @NotBlank(message = "X-Request-Id不能为空") @RequestHeader("X-Request-Id") String requestId,
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "登录方式不能为空") @RequestHeader("LoginType") String loginType,
            @Parameter(content = @Content(mediaType = "multipart/form-data")) @ModelAttribute CreatGroupVO creatGroupVO
    ) {
        Integer type = Convert.toInt(loginType);

        return groupService.createGroup(token, type, creatGroupVO);
    }

    @Operation(
            summary = "查询我加入的群聊",
            description = "查询当前登录用户加入的所有群聊列表，包括群聊基本信息和成员数量等。",
            method = "GET"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @GetMapping
    public R<List<MyGroupVO>> queryMyGroups(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "登录方式不能为空") @RequestHeader("LoginType") String loginType
    ) {
        Integer type = Convert.toInt(loginType);

        return groupService.queryMyGroups(token, type);
    }

    // 查询群组信息
    @Operation(
            summary = "查询群组信息",
            description = "根据群组ID查询群组的详细信息，包括群聊名称、公告、成员数量、创建时间等。",
            method = "GET"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "groupId", description = "群组ID", required = true, in = ParameterIn.PATH)
    })
    @VerificationInterceptor(checkLogin = true)
    @GetMapping("/{groupId}")
    public R<GroupInfoVO> queryGroupInfo(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "登录方式不能为空") @RequestHeader("LoginType") String loginType,
            @NotBlank(message = "群组Id不能为空") @PathVariable String groupId) {
        return groupService.queryGroupInfo(groupId);
    }
}
