package course.QTalk.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.Builder;
import lombok.Data;

/**
 * QT群组成员表
 * @TableName qt_group_member
 */
@TableName(value ="qt_group_member")
@Builder
@Data
public class QtGroupMember {
    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 群组ID
     */
    private String groupId;

    /**
     * 用户UID
     */
    private String userUid;

    /**
     * 角色(1:普通成员, 2:管理员, 3:群主)
     */
    private Integer role;

    /**
     * 群内昵称
     */
    private String alias;

    /**
     * 是否置顶(0:否, 1:是)
     */
    private Integer isTop;

    /**
     * 是否免打扰(0:否, 1:是)
     */
    private Integer isDisturb;

    /**
     * 最后阅读消息序列号
     */
    private Long lastReadSeq;

    /**
     * 禁言截止时间(NULL表示未禁言)
     */
    private Date muteEndTime;

    /**
     * 入群方式(1:邀请, 2:搜索, 3:二维码)
     */
    private Integer joinType;

    /**
     * 是否退出(0:否, 1:是)
     */
    private Integer isQuit;

    /**
     * 入群时间
     */
    private Date joinTime;

    /**
     * 离群时间(NULL表示未离群)
     */
    private Date leaveTime;

    /**
     * 扩展字段
     */
    private String extra;
}