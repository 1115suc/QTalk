package course.QTalk.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 消息表
 * @TableName chat_message
 */
@TableName(value ="chat_message")
public class ChatMessage {
    /**
     * 消息自增 id
     */
    @TableId(type = IdType.AUTO)
    private Long messageId;

    /**
     * 会话 id
     */
    private String sessionId;

    /**
     * 消息类型
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 发送人 id
     */
    private String sendUserId;

    /**
     * 发送人昵称
     */
    private String sendUserNickname;

    /**
     * 发送时间
     */
    private Long sendTime;

    /**
     * 接收人 id
     */
    private String contactId;

    /**
     * 接收人类型 (0:好友 1:群聊)
     */
    private Integer contactType;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private Integer fileType;

    /**
     * 状态 (0:正在发送 1:已发送)
     */
    private Integer status;

    /**
     * 消息自增 id
     */
    public Long getMessageId() {
        return messageId;
    }

    /**
     * 消息自增 id
     */
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    /**
     * 会话 id
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 会话 id
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 消息类型
     */
    public Integer getMessageType() {
        return messageType;
    }

    /**
     * 消息类型
     */
    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    /**
     * 消息内容
     */
    public String getMessageContent() {
        return messageContent;
    }

    /**
     * 消息内容
     */
    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    /**
     * 发送人 id
     */
    public String getSendUserId() {
        return sendUserId;
    }

    /**
     * 发送人 id
     */
    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    /**
     * 发送人昵称
     */
    public String getSendUserNickname() {
        return sendUserNickname;
    }

    /**
     * 发送人昵称
     */
    public void setSendUserNickname(String sendUserNickname) {
        this.sendUserNickname = sendUserNickname;
    }

    /**
     * 发送时间
     */
    public Long getSendTime() {
        return sendTime;
    }

    /**
     * 发送时间
     */
    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    /**
     * 接收人 id
     */
    public String getContactId() {
        return contactId;
    }

    /**
     * 接收人 id
     */
    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    /**
     * 接收人类型 (0:好友 1:群聊)
     */
    public Integer getContactType() {
        return contactType;
    }

    /**
     * 接收人类型 (0:好友 1:群聊)
     */
    public void setContactType(Integer contactType) {
        this.contactType = contactType;
    }

    /**
     * 文件名
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 文件名
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 文件大小
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * 文件大小
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 文件类型
     */
    public Integer getFileType() {
        return fileType;
    }

    /**
     * 文件类型
     */
    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    /**
     * 状态 (0:正在发送 1:已发送)
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 状态 (0:正在发送 1:已发送)
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
}