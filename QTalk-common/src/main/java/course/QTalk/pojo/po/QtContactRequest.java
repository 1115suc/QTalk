package course.QTalk.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * QT联系人申请表
 * @TableName qt_contact_request
 */
@TableName(value ="qt_contact_request")
@Data
public class QtContactRequest {
    /**
     * 自增主键
     */
    @TableId
    private Long id;

    /**
     * 发送方UID
     */
    private String fromUid;

    /**
     * 接收方ID
     */
    private String toId;

    /**
     * 接收方类型(1:用户, 2:群组)
     */
    private Integer toType;

    /**
     * 联系人ID（用户：好友UID，群组：群主和管理员的UID）
     */
    private String contactId;

    /**
     * 申请理由
     */
    private String reason;

    /**
     * 状态(0:待处理, 1:已同意, 2:已拒绝, 3:已忽略)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 处理时间
     */
    private Date handleTime;
}