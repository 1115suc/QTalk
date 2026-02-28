package course.QTalk.pojo.po;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import lombok.Builder;
import lombok.Data;

/**
 * QT群组信息表
 * @TableName qt_group
 */
@TableName(value ="qt_group")
@Builder
@Data
public class QtGroup {
    /**
     * 自增主键
     */
    @TableId
    private Long id;

    /**
     * 群组公开ID（业务主键）
     */
    private String groupId;

    /**
     * 群组名称
     */
    private String name;

    /**
     * 群组头像URL
     */
    private String avatar;

    /**
     * 群主UID（关联sys_user.uid）
     */
    private String ownerUid;

    /**
     * 群组公告
     */
    private String notice;

    /**
     * 最大成员数
     */
    private Integer maxCount;

    /**
     * 当前成员数
     */
    private Integer currentCount;

    /**
     * 是否允许普通成员邀请(0:否, 1:是)
     */
    private Integer allowInvite;

    /**
     * 入群方式(0:同意后加入, 1:直接加入, 2:邀请加入, 3:拒绝任何人加入)
     */
    private Integer joinType;

    /**
     * 群组状态(0:正常, 1:解散, 2:封禁)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 扩展字段（JSON格式，预留未来功能）
     */
    private String extra;
}