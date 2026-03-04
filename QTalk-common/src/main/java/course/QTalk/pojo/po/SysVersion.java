package course.QTalk.pojo.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

/**
 * 系统版本管理表
 * @TableName sys_version
 */
@TableName(value ="sys_version")
public class SysVersion {
    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 版本号 (如：1.0.0)
     */
    private String version;

    /**
     * 平台类型 (1:Web 端 2:Android 3: iOS)
     */
    private Integer platform;

    /**
     * 更新描述 (支持 HTML/Markdown)
     */
    private String updateDesc;

    /**
     * 安装包/更新文件 URL
     */
    private String fileUrl;

    /**
     * 文件大小 (字节)
     */
    private Long fileSize;

    /**
     * 文件 MD5 校验值
     */
    private String fileMd5;

    /**
     * 文件类型 (1:完整包 2:增量包 3:外部分发链接)
     */
    private Integer fileType;

    /**
     * 外部下载链接 (如第三方存储)
     */
    private String outerLink;

    /**
     * 状态 (0:未发布, 1:全网发布, 2:灰度发布)
     */
    private Integer status;

    /**
     * 灰度测试用户 UID 列表 (逗号分隔)
     */
    private String grayscaleUids;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 主键 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 版本号 (如：1.0.0)
     */
    public String getVersion() {
        return version;
    }

    /**
     * 版本号 (如：1.0.0)
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 平台类型 (1:Web 端 2:Android 3: iOS)
     */
    public Integer getPlatform() {
        return platform;
    }

    /**
     * 平台类型 (1:Web 端 2:Android 3: iOS)
     */
    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    /**
     * 更新描述 (支持 HTML/Markdown)
     */
    public String getUpdateDesc() {
        return updateDesc;
    }

    /**
     * 更新描述 (支持 HTML/Markdown)
     */
    public void setUpdateDesc(String updateDesc) {
        this.updateDesc = updateDesc;
    }

    /**
     * 安装包/更新文件 URL
     */
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * 安装包/更新文件 URL
     */
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    /**
     * 文件大小 (字节)
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * 文件大小 (字节)
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 文件 MD5 校验值
     */
    public String getFileMd5() {
        return fileMd5;
    }

    /**
     * 文件 MD5 校验值
     */
    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    /**
     * 文件类型 (1:完整包 2:增量包 3:外部分发链接)
     */
    public Integer getFileType() {
        return fileType;
    }

    /**
     * 文件类型 (1:完整包 2:增量包 3:外部分发链接)
     */
    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    /**
     * 外部下载链接 (如第三方存储)
     */
    public String getOuterLink() {
        return outerLink;
    }

    /**
     * 外部下载链接 (如第三方存储)
     */
    public void setOuterLink(String outerLink) {
        this.outerLink = outerLink;
    }

    /**
     * 状态 (0:未发布, 1:全网发布, 2:灰度发布)
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 状态 (0:未发布, 1:全网发布, 2:灰度发布)
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 灰度测试用户 UID 列表 (逗号分隔)
     */
    public String getGrayscaleUids() {
        return grayscaleUids;
    }

    /**
     * 灰度测试用户 UID 列表 (逗号分隔)
     */
    public void setGrayscaleUids(String grayscaleUids) {
        this.grayscaleUids = grayscaleUids;
    }

    /**
     * 发布时间
     */
    public Date getPublishTime() {
        return publishTime;
    }

    /**
     * 发布时间
     */
    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }
}