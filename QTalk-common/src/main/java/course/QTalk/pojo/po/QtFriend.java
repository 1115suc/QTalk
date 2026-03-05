package course.QTalk.pojo.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * QT好友关系表
 * @TableName qt_friend
 */
@TableName(value ="qt_friend")
@Data
public class QtFriend {
    /**
     * 自增主键
     */
    @TableId
    private Long id;

    /**
     * 用户UID
     */
    private String userUid;

    /**
     * 好友UID
     */
    private String friendUid;

    /**
     * 好友备注
     */
    private String remark;

    /**
     * 来源(1:搜索, 2:群聊, 3:名片, 4:扫码)
     */
    private Integer source;

    /**
     * 状态(0:正常, 1:删除, 2:拉黑)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 扩展字段
     */
    private String extra;
}