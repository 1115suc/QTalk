package course.QTalk.mapper;

import course.QTalk.pojo.bo.GroupMemberInfoBO;
import course.QTalk.pojo.po.QtGroupMember;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 32147
* @description 针对表【qt_group_member(QT群组成员表)】的数据库操作Mapper
* @createDate 2026-02-28 11:32:12
* @Entity course.QTalk.pojo.po.QtGroupMember
*/
public interface QtGroupMemberMapper extends BaseMapper<QtGroupMember> {

    List<GroupMemberInfoBO> selectGroupMembersInfo(@Param("groupId") String groupId);
}




