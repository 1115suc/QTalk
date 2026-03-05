package course.QTalk.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 联系人表
 * @TableName chat_session_user
 */
@TableName(value ="chat_session_user")
public class ChatSessionUser {
    /**
     * 用户 id
     */
    @TableId(type = IdType.INPUT)
    private String uid;

    /**
     * 联系人 id
     */
    private String contactId;

    /**
     * 会话 id
     */
    private String sessionId;

    /**
     * 联系人名称
     */
    private String contactName;

    /**
     * 最后接收到的消息
     */
    private String lastMessage;

    /**
     * 最后接收到的消息（毫秒）
     */
    private Long lastReceiveTime;

    /**
     * 用户 id
     */
    public String getUid() {
        return uid;
    }

    /**
     * 用户 id
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * 联系人 id
     */
    public String getContactId() {
        return contactId;
    }

    /**
     * 联系人 id
     */
    public void setContactId(String contactId) {
        this.contactId = contactId;
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
     * 联系人名称
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * 联系人名称
     */
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    /**
     * 最后接收到的消息
     */
    public String getLastMessage() {
        return lastMessage;
    }

    /**
     * 最后接收到的消息
     */
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    /**
     * 最后接收到的消息（毫秒）
     */
    public Long getLastReceiveTime() {
        return lastReceiveTime;
    }

    /**
     * 最后接收到的消息（毫秒）
     */
    public void setLastReceiveTime(Long lastReceiveTime) {
        this.lastReceiveTime = lastReceiveTime;
    }
}