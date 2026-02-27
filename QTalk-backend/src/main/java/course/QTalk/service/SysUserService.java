package course.QTalk.service;

import course.QTalk.pojo.po.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import course.QTalk.pojo.vo.request.EmailLoginVo;
import course.QTalk.pojo.vo.response.CheckCodeVo;
import course.QTalk.pojo.vo.response.R;

/**
* @author 1115suc
* @description 针对表【sys_user(用户表)】的数据库操作Service
* @createDate 2026-02-25 12:22:35
*/
public interface SysUserService extends IService<SysUser> {
    // 获取验证码
    R<CheckCodeVo> getCaptcha();
    // 用户注册接口
    R<String> register(EmailLoginVo emailLoginVo);
}
