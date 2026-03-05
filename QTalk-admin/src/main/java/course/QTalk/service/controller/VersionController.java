package course.QTalk.service.controller;

import cn.hutool.core.convert.Convert;
import course.QTalk.annotation.VerificationInterceptor;
import course.QTalk.pojo.po.SysVersion;
import course.QTalk.pojo.vo.request.UploadVersionVO;
import course.QTalk.pojo.vo.response.LoadVersionVO;
import course.QTalk.pojo.vo.response.R;
import course.QTalk.service.service.SysVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("/version")
@RequiredArgsConstructor
@Tag(name = "项目版本管理", description = "项目版本管理接口")
public class VersionController {

    private final SysVersionService sysVersionService;

    @Operation(
            summary = "加载版本信息",
            method = "GET"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
    })
    @VerificationInterceptor(checkLogin = true)
    @GetMapping("/load")
    public R<List<LoadVersionVO>> loadVersion(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "登录方式不能为空") @RequestHeader("LoginType") String loginType
    ) {
        Integer type = Convert.toInt(loginType);
        return sysVersionService.loadVersion(token, type);
    }

    // 上传版本
    @Operation(
            summary = "上传版本",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping
    public R<String> uploadVersion(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "登录方式不能为空") @RequestHeader("LoginType") String loginType,
            @RequestBody UploadVersionVO uploadVersionVO,
            MultipartFile file
    ){
        Integer type = Convert.toInt(loginType);
        return sysVersionService.uploadVersion(token, type, uploadVersionVO, file);
    }
}
