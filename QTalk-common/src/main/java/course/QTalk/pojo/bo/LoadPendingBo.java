package course.QTalk.pojo.bo;

import lombok.Data;

import java.util.Date;

@Data
public class LoadPendingBo {

    private String fromUid;

    private Integer toType;

    private String reason;

    private Integer status;

    private Date createTime;

    private String nickName;

    private String avatar;
}
