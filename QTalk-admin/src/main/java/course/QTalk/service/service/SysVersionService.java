package course.QTalk.service.service;

import course.QTalk.pojo.po.SysVersion;
import com.baomidou.mybatisplus.extension.service.IService;
import course.QTalk.pojo.vo.request.UploadVersionVO;
import course.QTalk.pojo.vo.response.LoadVersionVO;
import course.QTalk.pojo.vo.response.R;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author 32147
* @description 针对表【sys_version(系统版本管理表)】的数据库操作Service
* @createDate 2026-03-04 15:48:07
*/
public interface SysVersionService extends IService<SysVersion> {

    R<List<LoadVersionVO>> loadVersion(String token, Integer type);

    R<String> uploadVersion(String token, Integer type, UploadVersionVO uploadVersionVO, MultipartFile file);
}
