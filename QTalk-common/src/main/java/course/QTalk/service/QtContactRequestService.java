package course.QTalk.service;

import course.QTalk.pojo.po.QtContactRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import course.QTalk.pojo.vo.request.ApplyJoinContactVO;
import course.QTalk.pojo.vo.request.GroupBasicInfoVO;
import course.QTalk.pojo.vo.request.HandleFormApplyVO;
import course.QTalk.pojo.vo.request.LoadPendingRequestsVO;
import course.QTalk.pojo.vo.request.UserSearchVO;
import course.QTalk.pojo.vo.response.GroupInfoVO;
import course.QTalk.pojo.vo.response.LoadPendingResponseVO;
import course.QTalk.pojo.vo.response.R;
import course.QTalk.pojo.vo.response.UserSearchInfoVO;

import java.util.List;

/**
 * @author 32147
 * @description 针对表【qt_contact_request(QT联系人申请表)】的数据库操作Service
 * @createDate 2026-02-28 11:32:12
 */
public interface QtContactRequestService extends IService<QtContactRequest> {
    // 搜索用户
    R<List<UserSearchInfoVO>> searchUser(UserSearchVO userSearchVO);
    // 搜索群聊
    R<List<GroupInfoVO>> queryGroupInfo(GroupBasicInfoVO groupBasicInfoVO);

    void applyAddFriend(String token, Integer type, ApplyJoinContactVO applyJoinContactVO);

    void applyJoinGroup(String token, Integer type, ApplyJoinContactVO applyJoinContactVO);
    // 加载待处理请求
    R<List<LoadPendingResponseVO>> loadPendingRequests(String token, Integer type, LoadPendingRequestsVO loadPendingRequestsVO);
    // 处理表单申请
    R handleFormApply(String token, Integer type, HandleFormApplyVO handleFormApplyVO);
}
