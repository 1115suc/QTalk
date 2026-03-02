package course.QTalk.service;

import course.QTalk.pojo.po.QtGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import course.QTalk.pojo.vo.request.CreatGroupVO;
import course.QTalk.pojo.vo.response.GroupInfoVO;
import course.QTalk.pojo.vo.response.MyGroupVO;
import course.QTalk.pojo.vo.response.R;

import java.util.List;

/**
* @author 32147
* @description 针对表【qt_group(QT群组信息表)】的数据库操作Service
* @createDate 2026-02-28 11:32:12
*/
public interface QtGroupService extends IService<QtGroup> {

    R createGroup(String token, Integer type, CreatGroupVO creatGroupVO);

    R<List<MyGroupVO>> queryMyGroups(String token, Integer type);

    R<GroupInfoVO> queryGroupInfo(String groupId);
}
