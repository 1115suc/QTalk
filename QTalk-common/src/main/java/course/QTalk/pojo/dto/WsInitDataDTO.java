package course.QTalk.pojo.dto;

import course.QTalk.pojo.po.ChatMessage;
import course.QTalk.pojo.po.ChatSessionUser;
import lombok.Data;

import java.util.List;

@Data
public class WsInitDataDTO {
    private List<ChatSessionUser> chatSessionUserList;

    private List<ChatMessage> chatMessageList;

    private Integer applyCount;
}
