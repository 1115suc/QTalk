package course.QTalk.pojo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageSendDto<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long messageId;
    private String sessionId;
    private String sendUserId;
    private String sendUserNickName;
    private String contactId;
    private String contactNickName;
    private String messageContent;
    private String lastMessage;
    private Integer messageType;
    private Long sendTime;
    private Integer contactType;
    private T extendData;
    private Integer status;
    private Long fileSize;
    private String fileName;
    private Integer fileType;
    private Integer memberCount;
}