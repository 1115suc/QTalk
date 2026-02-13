package course.QTalk.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "测试接口", description = "测试相关的接口")
@Validated
@RestController
@RequestMapping("/test")
public class testController {

    @Operation(summary = "测试hello接口", description = "返回问候信息", method = "POST")
    @Parameter(name = "name", description = "用户名", required = true, example = "张三")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    @PostMapping("/hello")
    public String hello(@NotNull(message = "name不能为空") String name) {
        return "hello QTalk " + name;
    }
}
