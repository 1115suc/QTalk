package course.QTalk.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QTalk.constant.CommonConstant;
import course.QTalk.constant.RedisConstant;
import course.QTalk.constant.TimeConstant;
import course.QTalk.exception.QTException;
import course.QTalk.exception.QTWebException;
import course.QTalk.mapper.QtContactRequestMapper;
import course.QTalk.mapper.QtFriendMapper;
import course.QTalk.mapper.QtGroupMemberMapper;
import course.QTalk.pojo.dto.TokenUserDTO;
import course.QTalk.pojo.enums.*;
import course.QTalk.pojo.po.QtContactRequest;
import course.QTalk.pojo.po.QtFriend;
import course.QTalk.pojo.po.SysUser;
import course.QTalk.pojo.vo.request.ApplyJoinContactVO;
import course.QTalk.pojo.vo.request.EmailCodeLoginVO;
import course.QTalk.pojo.vo.request.EmailLoginVO;
import course.QTalk.pojo.vo.request.EmailPasswordLoginVO;
import course.QTalk.pojo.vo.request.ResetPasswordVO;
import course.QTalk.pojo.vo.request.UpdateUserInfoVO;
import course.QTalk.pojo.vo.request.UserSearchVO;
import course.QTalk.pojo.vo.response.CheckCodeVo;
import course.QTalk.pojo.vo.response.R;
import course.QTalk.pojo.enums.ResponseCode;
import course.QTalk.pojo.vo.response.UserLoginVO;
import course.QTalk.pojo.vo.response.UserSearchInfoVO;
import course.QTalk.service.EmailCodeService;
import course.QTalk.service.SysUserService;
import course.QTalk.mapper.SysUserMapper;
import course.QTalk.util.IdWorker;
import course.QTalk.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.util.Date;

import static course.QTalk.service.base.BaseService.verifyCheckCode;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.bean.BeanUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 1115suc
* @description 针对表【sys_user(用户表)】的数据库操作Service实现
* @createDate 2026-02-25 12:22:35
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService{

    private final IdWorker idWorker;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;
    private final EmailCodeService emailCodeService;
    private final SysUserMapper sysUserMapper;
    private final QtFriendMapper qtFriendMapper;
    private final QtContactRequestMapper qtContactRequestMapper;

    @Override
    public R<CheckCodeVo> getCaptcha() {
        // 自定义纯数字的验证码（随机4位数字，可重复）
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 5);
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100);
        lineCaptcha.setGenerator(randomGenerator);
        lineCaptcha.setFont(new Font("Arial", Font.BOLD, 50));
        lineCaptcha.setBackground(new Color(255, 255, 255));
        // 生成code
        lineCaptcha.createCode();
        String checkCode = lineCaptcha.getCode();

        log.info("生成校验码:{}", checkCode);

        //生成sessionId
        String sessionId = String.valueOf(idWorker.nextId());
        redisUtil.set(RedisConstant.CAPTCHA_KEY + sessionId, checkCode, TimeConstant.FIVE_MINUTE);
        CheckCodeVo checkCodeVo = new CheckCodeVo();
        checkCodeVo.setSessionId(sessionId);
        checkCodeVo.setImageData(lineCaptcha.getImageBase64Data());
        if (ObjectUtil.isNotEmpty(checkCodeVo)) {
            return R.ok(checkCodeVo);
        } else {
            log.error("生成校验码失败!!");
            throw new QTException(ResponseCode.CHECK_CODE_GENERATE_ERROR.getMessage());
        }
    }

    @Override
    public R register(EmailLoginVO emailLoginVo) {
        // 判断邮箱是否已存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        String email = emailLoginVo.getEmail();
        queryWrapper.eq(SysUser::getEmail, email);
        if (ObjectUtil.isNotNull(sysUserMapper.selectOne(queryWrapper))) {
            throw new QTWebException(ResponseCode.ACCOUNT_EXISTS_ERROR.getMessage());
        }

        // 获取邮箱验证码
        if (!redisUtil.hasKey(RedisConstant.EMAIL_CODE + email)) {
            throw new QTWebException(ResponseCode.CHECK_CODE_EXPIRED_WAIT.getMessage());
        }
        emailCodeService.checkCode(email, emailLoginVo.getEmailCode());
        SysUser sysUser = new SysUser();

        String uid = "U" + RandomUtil.randomNumbers(9);
        sysUser.setEmail(emailLoginVo.getEmail());
        sysUser.setUid(uid);
        sysUser.setPassword(passwordEncoder.encode(emailLoginVo.getPassword()));
        sysUser.setNickName(emailLoginVo.getNickName());
        sysUser.setAddFriends(AddFriendsEnum.AGREE_ADD_FRIEND.getCode());
        sysUser.setSex(SexEnum.UNKNOWN.getCode());
        sysUser.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        sysUser.setStatus(StatusEnum.NORMAL.getCode());
        sysUser.setCreateWhere(emailLoginVo.getCreateWhere());
        sysUser.setCreateTime(new Date(System.currentTimeMillis()));
        sysUser.setUpdateTime(new Date(System.currentTimeMillis()));

        try {
            sysUserMapper.insert(sysUser);
            return R.ok(ResponseCode.REGISTER_SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("用户注册失败:{}", e.getMessage());
            return R.error(ResponseCode.REGISTER_ERROR.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<UserLoginVO> emailPasswordLogin(EmailPasswordLoginVO emailPasswordLoginVo) {
        String checkCode = emailPasswordLoginVo.getCheckCode();
        String sessionId = emailPasswordLoginVo.getSessionId();

        verifyCheckCode(checkCode, sessionId, redisUtil);

        return processLogin(emailPasswordLoginVo.getEmail(), emailPasswordLoginVo.getLoginWhere(), emailPasswordLoginVo.getPassword());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<UserLoginVO> emailCodeLogin(EmailCodeLoginVO emailCodeLoginVo) {
        String email = emailCodeLoginVo.getEmail();
        String code = emailCodeLoginVo.getEmailCode();

        // 校验验证码
        if (!redisUtil.hasKey(RedisConstant.EMAIL_CODE + email)) {
            throw new QTWebException(ResponseCode.CHECK_CODE_EXPIRED_WAIT.getMessage());
        }
        emailCodeService.checkCode(email, code);

        return processLogin(email, emailCodeLoginVo.getLoginWhere(), null);
    }

    private R<UserLoginVO> processLogin(String email, Integer loginWhere, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getEmail, email);
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);

        // 账号不存在
        if (ObjectUtil.isNull(sysUser) || NumberUtil.equals(sysUser.getDeleted(), DeletedEnum.DELETED.getCode())) {
            throw new QTWebException(ResponseCode.ACCOUNT_NOT_EXISTS.getMessage());
        }
        // 账号被禁用
        Integer status = sysUser.getStatus();
        if (NumberUtil.equals(status, StatusEnum.DISABLED.getCode())) {
            throw new QTWebException(ResponseCode.ACCOUNT_LOCKED.getMessage());
        }

        // 密码校验 (仅当传入密码时)
        if (password != null && !passwordEncoder.matches(password, sysUser.getPassword())) {
            throw new QTWebException(ResponseCode.PASSWORD_ERROR.getMessage());
        }

        String redisPrefix = LoginTypeEnum.of(loginWhere).getPrefix();
        String token = DigestUtil.md5Hex(sysUser.getUid());

        try {
            if (redisUtil.hasKey(redisPrefix + token)) {
                // 获取登录信息
                Object tokenLoginInfo = redisUtil.get(redisPrefix + token);
                TokenUserDTO tokenUserDTO = JSONUtil.toBean(tokenLoginInfo.toString(), TokenUserDTO.class);
                // 判断时间是否快要过期
                if (tokenUserDTO.getExpireAt() - System.currentTimeMillis() < TimeConstant.ONE_DAY) {
                    tokenUserDTO.setExpireAt(System.currentTimeMillis() + TimeConstant.ONE_WEEK);
                    redisUtil.set(redisPrefix + token, JSONUtil.toJsonStr(tokenUserDTO), TimeConstant.ONE_WEEK);
                }
            }else{
                TokenUserDTO tokenUserDTO = new TokenUserDTO();
                tokenUserDTO.setUid(sysUser.getUid());
                tokenUserDTO.setNickname(sysUser.getNickName());
                tokenUserDTO.setLoginWhere(loginWhere);
                tokenUserDTO.setExpireAt(System.currentTimeMillis() + TimeConstant.ONE_WEEK);
                tokenUserDTO.setToken(token);
                String tokenLoginInfo = JSONUtil.toJsonStr(tokenUserDTO);
                redisUtil.set(redisPrefix + token, tokenLoginInfo, TimeConstant.ONE_WEEK);
            }

            sysUser.setLastLoginTime(new Date(System.currentTimeMillis()));
            sysUserMapper.updateById(sysUser);

            UserLoginVO userLoginVO = new UserLoginVO();
            userLoginVO.setUid(sysUser.getUid());
            userLoginVO.setNickname(sysUser.getNickName());
            userLoginVO.setAvatar(sysUser.getAvatar());
            userLoginVO.setToken(token);
            return R.ok(ResponseCode.LOGIN_SUCCESS.getMessage(), userLoginVO);
        } catch (Exception e) {
            throw new QTWebException(ResponseCode.LOGIN_ERROR.getMessage());
        }
    }

    @Override
    public R logout(String token, Integer loginType) {
        try {
            redisUtil.del(LoginTypeEnum.of(loginType).getPrefix() + token);
            return R.ok(ResponseCode.LOGOUT_SUCCESS.getMessage());
        } catch (Exception e) {
            throw new QTWebException(ResponseCode.ERROR.getMessage());
        }

    }

    @Override
    public R resetPassword(String token, Integer loginType, ResetPasswordVO resetVo) {
        String emailCode = resetVo.getEmailCode();
        emailCodeService.checkCode(resetVo.getEmail(), emailCode);

        String redisPrefix = LoginTypeEnum.of(loginType).getPrefix();
        try {
            Object tokenLoginInfo = redisUtil.get(redisPrefix + token);
            TokenUserDTO tokenUserDTO = JSONUtil.toBean(tokenLoginInfo.toString(), TokenUserDTO.class);

            // 获取用户信息
            String uid = tokenUserDTO.getUid();
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getUid, uid);
            SysUser sysUser = sysUserMapper.selectOne(queryWrapper);
            if (ObjectUtil.isNull(sysUser)) {
                throw new QTWebException(ResponseCode.ACCOUNT_NOT_EXISTS.getMessage());
            }

            sysUser.setPassword(passwordEncoder.encode(resetVo.getNewPassword()));
            sysUserMapper.updateById(sysUser);
            if (redisUtil.hasKey(LoginTypeEnum.of(1).getPrefix() + token)) {
                redisUtil.del(LoginTypeEnum.of(1).getPrefix() + token);
            }
            if (redisUtil.hasKey(LoginTypeEnum.of(2).getPrefix() + token)) {
                redisUtil.del(LoginTypeEnum.of(2).getPrefix() + token);
            }
            if (redisUtil.hasKey(LoginTypeEnum.of(3).getPrefix() + token)) {
                redisUtil.del(LoginTypeEnum.of(3).getPrefix() + token);
            }

            return R.ok(ResponseCode.RESET_PASSWORD_SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("获取用户token信息失败:{}, 登录方式:{}", e.getMessage(), loginType);
            throw new QTWebException(ResponseCode.ERROR.getMessage());
        }
    }

    @Override
    public R updateUserInfo(String token, Integer loginType, UpdateUserInfoVO updateUserInfoVO) {
        String redisPrefix = LoginTypeEnum.of(loginType).getPrefix();
        try {
            Object tokenLoginInfo = redisUtil.get(redisPrefix + token);
            TokenUserDTO tokenUserDTO = JSONUtil.toBean(tokenLoginInfo.toString(), TokenUserDTO.class);

            // 获取用户信息
            String uid = tokenUserDTO.getUid();
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getUid, uid);
            SysUser sysUser = sysUserMapper.selectOne(queryWrapper);
            if (ObjectUtil.isNull(sysUser)) {
                throw new QTWebException(ResponseCode.ACCOUNT_NOT_EXISTS.getMessage());
            }

            // 更新用户信息
            if (ObjectUtil.isNotNull(updateUserInfoVO.getNickName())) {
                sysUser.setNickName(updateUserInfoVO.getNickName());
            }
            if (ObjectUtil.isNotNull(updateUserInfoVO.getAvatar())) {
                sysUser.setAvatar(updateUserInfoVO.getAvatar());
            }
            if (ObjectUtil.isNotNull(updateUserInfoVO.getDescription())) {
                sysUser.setDescription(updateUserInfoVO.getDescription());
            }
            if (ObjectUtil.isNotNull(updateUserInfoVO.getBirthday())) {
                sysUser.setBirthday(updateUserInfoVO.getBirthday());
            }
            if (ObjectUtil.isNotNull(updateUserInfoVO.getAddFriends())) {
                sysUser.setAddFriends(updateUserInfoVO.getAddFriends());
            }
            if (ObjectUtil.isNotNull(updateUserInfoVO.getSex())) {
                sysUser.setSex(updateUserInfoVO.getSex());
            }
            if (ObjectUtil.isNotNull(updateUserInfoVO.getAreaName())) {
                sysUser.setAreaName(updateUserInfoVO.getAreaName());
            }
            // 更新时间
            sysUser.setUpdateTime(new Date(System.currentTimeMillis()));

            sysUserMapper.updateById(sysUser);
            return R.ok("用户信息更新成功");
        } catch (Exception e) {
            log.error("更新用户信息失败:{}, 登录方式:{}", e.getMessage(), loginType);
            throw new QTWebException(ResponseCode.ERROR.getMessage());
        }
    }

    @Override
    public R<List<UserSearchInfoVO>> searchUser(UserSearchVO userSearchVO) {
        String uid = userSearchVO.getUid();
        String nickName = userSearchVO.getNickName();
        String email = userSearchVO.getEmail();

        if (StrUtil.isBlank(uid) && StrUtil.isBlank(nickName) && StrUtil.isBlank(email)) {
            return R.ok(ResponseCode.PARAM_NOT_EMPTY.getMessage());
        }

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        // 过滤已删除用户
        queryWrapper.eq(SysUser::getDeleted, DeletedEnum.NOT_DELETED.getCode());

        // 构造搜索条件: uid精确匹配 OR email精确匹配 OR nickName模糊匹配
        queryWrapper.and(wrapper -> wrapper
                .eq(SysUser::getUid, uid)
                .or()
                .eq(SysUser::getEmail, email)
                .or()
                .like(SysUser::getNickName, nickName)
        );

        List<SysUser> sysUsers = sysUserMapper.selectList(queryWrapper);

        if (CollectionUtil.isEmpty(sysUsers)) {
            return R.ok(ResponseCode.CONTACT_NOT_EXISTS.getMessage());
        }

        List<UserSearchInfoVO> userSearchInfoVOS = sysUsers.stream().map(sysUser -> {
            UserSearchInfoVO vo = new UserSearchInfoVO();
            BeanUtil.copyProperties(sysUser, vo);
            return vo;
        }).collect(Collectors.toList());

        return R.ok(userSearchInfoVOS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyAddFriend(String token, Integer type, ApplyJoinContactVO applyJoinContactVO) {
        String redisPrefix = LoginTypeEnum.of(type).getPrefix();
        Object tokenLoginInfo = redisUtil.get(redisPrefix + token);
        TokenUserDTO tokenUserDTO = JSONUtil.toBean(tokenLoginInfo.toString(), TokenUserDTO.class);

        String applyId = applyJoinContactVO.getApplyId();
        if (!applyId.startsWith("U")) {
            throw new QTWebException(ResponseCode.USER_ID_ERROR.getMessage());
        }

        String fromUid = tokenUserDTO.getUid();
        String toUid = applyJoinContactVO.getApplyId();

        // 1. 校验目标用户是否存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUid, toUid);
        queryWrapper.eq(SysUser::getDeleted, DeletedEnum.NOT_DELETED.getCode());
        SysUser toUser = sysUserMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNull(toUser)) {
            throw new QTWebException(ResponseCode.ACCOUNT_NOT_EXISTS.getMessage());
        }

        // 2. 校验是否允许加好友
        if (toUser.getAddFriends().equals(AddFriendsEnum.NOT_ALLOW_ADD_FRIEND.getCode())) {
            throw new QTWebException("对方设置了不允许添加好友");
        }

        // 3. 校验是否已经是好友
        LambdaQueryWrapper<QtFriend> friendQuery = new LambdaQueryWrapper<>();
        friendQuery.eq(QtFriend::getUserUid, fromUid);
        friendQuery.eq(QtFriend::getFriendUid, toUid);
        QtFriend friend = qtFriendMapper.selectOne(friendQuery);
        if (ObjectUtil.isNotNull(friend) && friend.getStatus().equals(CommonConstant.ZERO)) {
            throw new QTWebException(ResponseCode.ALREADY_FRIEND.getMessage());
        }

        // 4. 检查是否已经申请过且未处理
        LambdaQueryWrapper<QtContactRequest> requestQuery = new LambdaQueryWrapper<>();
        requestQuery.eq(QtContactRequest::getFromUid, fromUid);
        requestQuery.eq(QtContactRequest::getToId, toUid);
        requestQuery.eq(QtContactRequest::getToType, ContactType.USER.getCode());
        requestQuery.eq(QtContactRequest::getStatus, ApplyStatus.PENDING.getCode());
        Long count = qtContactRequestMapper.selectCount(requestQuery);
        if (count > 0) {
            throw new QTWebException(ResponseCode.DUPLICATE_REQUEST.getMessage());
        }

        // 5. 构造申请理由
        String reason = applyJoinContactVO.getApplyReason();
        if (StrUtil.isBlank(reason)) {
            reason = String.format(CommonConstant.APPLY_REASON_TEMPLATE, tokenUserDTO.getNickname());
        }

        // 6. 插入申请记录
        QtContactRequest contactRequest = new QtContactRequest();
        contactRequest.setFromUid(fromUid);
        contactRequest.setToId(toUid);
        contactRequest.setToType(ContactType.USER.getCode());
        contactRequest.setContactId(toUid); // 对于好友申请，contactId即为对方UID
        contactRequest.setReason(reason);
        contactRequest.setStatus(ApplyStatus.PENDING.getCode());
        contactRequest.setCreateTime(new Date());

        qtContactRequestMapper.insert(contactRequest);
    }
}






