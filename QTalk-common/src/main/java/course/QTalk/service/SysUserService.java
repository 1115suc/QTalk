package course.QTalk.service;

import course.QTalk.pojo.po.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import course.QTalk.pojo.vo.request.EmailCodeLoginVO;
import course.QTalk.pojo.vo.request.EmailLoginVO;
import course.QTalk.pojo.vo.request.EmailPasswordLoginVO;
import course.QTalk.pojo.vo.request.ResetPasswordVO;
import course.QTalk.pojo.vo.request.UpdateUserInfoVO;
import course.QTalk.pojo.vo.response.CheckCodeVo;
import course.QTalk.pojo.vo.response.R;
import course.QTalk.pojo.vo.response.UserLoginVO;

/**
* @author 1115suc
* @description 针对表【sys_user(用户表)】的数据库操作Service
* @createDate 2026-02-25 12:22:35
*/
public interface SysUserService extends IService<SysUser> {
    // 获取验证码
    R<CheckCodeVo> getCaptcha();
    // 用户注册接口
    R register(EmailLoginVO emailLoginVo);
    // 更新用户信息
    R updateUserInfo(String token, String type, UpdateUserInfoVO updateUserInfoVO);
    // 邮箱密码登录
    R<UserLoginVO> emailPasswordLogin(EmailPasswordLoginVO emailPasswordLoginVo);
    // 邮箱验证码登录
    R<UserLoginVO> emailCodeLogin(EmailCodeLoginVO emailCodeLoginVo);
    // 登出
    R logout(String token, String loginType);
    // 重置密码
    R resetPassword(String token, String loginType, ResetPasswordVO resetVo);
}
