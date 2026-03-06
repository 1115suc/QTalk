package course.QTalk.controller;

import course.QTalk.annotation.VerificationInterceptor;
import course.QTalk.pojo.dto.MessageSendDto;
import course.QTalk.pojo.vo.request.SendFileVO;
import course.QTalk.pojo.vo.request.SendMessageVO;
import course.QTalk.pojo.vo.response.R;
import course.QTalk.service.ChatMessageService;
import course.QTalk.service.ChatSessionUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.service.GenericParameterService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "聊天管理", description = "聊天相关接口，包括发送消息，上传文件等功能")
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatSessionUserService chatSessionUserService;
    private final GenericParameterService parameterBuilder;

    // 发送消息
    @Operation(
            summary = "发送消息",
            description = "发送消息",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping("/sendMessage")
    public R<MessageSendDto> sendMessage(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "登录方式不能为空") @RequestHeader("LoginType") String loginType,
            @RequestBody SendMessageVO sendMessageVO
    ) {
        return chatMessageService.sendMessage(token, loginType, sendMessageVO);
    }

    // 发送文件
    @Operation(
            summary = "发送文件",
            description = "发送文件",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping("/sendFile")
    public R sendFile(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "登录方式不能为空") @RequestHeader("LoginType") String loginType,
            @RequestBody SendFileVO sendFileVO
    ) {
        return chatMessageService.sendFile(token, loginType, sendFileVO);
    }

    // 下载文件
    @Operation(
            summary = "下载文件",
            description = "下载文件",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping("/downloadFile")
    public R downloadFile(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "登录方式不能为空") @RequestHeader("LoginType") String loginType,
            @NotBlank(message = "消息ID不能为空") String messageId
    ) {
        return chatMessageService.downloadFile(token, loginType, messageId);
    }
}
