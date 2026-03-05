package course.QTalk.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Schema
@Data
public class LoadVersionVO {

    @Schema(description = "版本号 (如：1.0.0)")
    private String version;

    @Schema(description = "平台类型 (1:Web 端 2:Android 3: iOS)")
    private Integer platform;

    @Schema(description = "更新描述 (支持 HTML/Markdown)")
    private String updateDesc;

    @Schema(description = "安装包/更新文件 URL")
    private String fileUrl;

    @Schema(description = "文件大小 (字节)")
    private Long fileSize;

    @Schema(description = "文件 MD5 校验值")
    private String fileMd5;

    @Schema(description = "文件类型 (1:完整包 2:增量包 3:外部分发链接)")
    private Integer fileType;

    @Schema(description = "外部下载链接 (如第三方存储)")
    private String outerLink;

    @Schema(description = "状态 (0:未发布, 1:全网发布, 2:灰度发布)")
    private Integer status;

    @Schema(description = "灰度测试用户 UID 列表 (逗号分隔)")
    private String grayscaleUids;

    @Schema(description = "发布时间")
    private Date publishTime;
}
