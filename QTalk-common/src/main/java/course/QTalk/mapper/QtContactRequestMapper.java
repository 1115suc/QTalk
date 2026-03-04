package course.QTalk.mapper;

import course.QTalk.pojo.bo.LoadPendingBo;
import course.QTalk.pojo.po.QtContactRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 32147
* @description 针对表【qt_contact_request(QT联系人申请表)】的数据库操作Mapper
* @createDate 2026-02-28 11:32:12
* @Entity course.QTalk.pojo.po.QtContactRequest
*/
public interface QtContactRequestMapper extends BaseMapper<QtContactRequest> {

    List<LoadPendingBo> selectPending(@Param("uid") String uid, @Param("type") Integer type, @Param("status") Integer status);
}




