package course.QTalk.controller;

import cn.hutool.core.convert.Convert;
import course.QTalk.annotation.Idempotent;
import course.QTalk.annotation.VerificationInterceptor;
import course.QTalk.pojo.vo.request.CreatGroupVO;
import course.QTalk.pojo.vo.response.MyGroupVO;
import course.QTalk.pojo.vo.response.R;
import course.QTalk.service.QtGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "创建群聊", description = "创建群聊并上传头像", method = "POST")
    @VerificationInterceptor(checkLogin = true)
    @Idempotent(expire = 10, message = "群组创建中，请勿重复提交")
    @PostMapping(consumes = "multipart/form-data")
    public R createGroup(
            @Parameter(description = "幂等键", required = true) @NotBlank(message = "X-Request-Id不能为空") @RequestHeader("X-Request-Id") String requestId,
            @Parameter(description = "用户Token", required = true) @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @Parameter(description = "登录方式", required = true) @NotBlank(message = "登录方式不能为空") @RequestHeader("LoginType") String loginType,
            @Parameter(description = "群聊信息表单", content = @Content(mediaType = "multipart/form-data")) @ModelAttribute CreatGroupVO creatGroupVO
    ) {
        Integer type = Convert.toInt(loginType);

        return groupService.createGroup(token, type, creatGroupVO);
    }

    // 查询我加入的群聊
    @Operation(summary = "查询我加入的群聊", description = "查询我加入的群聊", method = "GET")
    @VerificationInterceptor(checkLogin = true)
    @GetMapping
    public R<List<MyGroupVO>> queryMyGroups(
            @Parameter(description = "用户Token", required = true) @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @Parameter(description = "登录方式", required = true) @NotBlank(message = "登录方式不能为空") @RequestHeader("LoginType") String loginType
    ) {
        Integer type = Convert.toInt(loginType);

        return groupService.queryMyGroups(token, type);
    }



}
