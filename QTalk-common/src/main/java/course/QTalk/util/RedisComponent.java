package course.QTalk.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import course.QTalk.constant.CommonConstant;
import course.QTalk.constant.RedisConstant;
import course.QTalk.exception.QTWebException;
import course.QTalk.pojo.dto.TokenUserDTO;
import course.QTalk.pojo.enums.LoginTypeEnum;
import course.QTalk.pojo.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisComponent {
    private final RedisUtil redisUtil;

    // 获取用户信息 TokenUserDTO
    public TokenUserDTO getTokenUserDTO(String LoginType, String token) {
        Integer type = Convert.toInt(LoginType);
        String loginPrefix = LoginTypeEnum.of(type).getPrefix();

        if (!redisUtil.hasKey(loginPrefix + token)) {
            throw new QTWebException(ResponseCode.LOGIN_TIMEOUT.getMessage());
        }

        String tokenLoginInfo = (String) redisUtil.get(loginPrefix + token);
        TokenUserDTO tokenUserDTO = JSONUtil.toBean(tokenLoginInfo.toString(), TokenUserDTO.class);
        return tokenUserDTO;
    }

    // 清空联系人
    public void cleanContactUser(String userId) {
       redisUtil.del(RedisConstant.FRIEND_LIST + userId);
    }

    // 添加联系人
    public List<Object> getContactUserList(String userId) {
        return redisUtil.lGet(RedisConstant.FRIEND_LIST + userId, CommonConstant.L_ZERO, CommonConstant.L_NEGATIVE_ONE);
    }

    // 清空群组
    public void cleanContactGroup(String groupId) {
        redisUtil.del(RedisConstant.GROUP_LIST + groupId);
    }

    // 添加群组
    public List<Object> getContactGroupList(String groupId) {
        return redisUtil.lGet(RedisConstant.GROUP_LIST + groupId, CommonConstant.L_ZERO, CommonConstant.L_NEGATIVE_ONE);
    }
}
