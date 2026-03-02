package course.QTalk.service.base;

import course.QTalk.constant.RedisConstant;
import course.QTalk.exception.QTWebException;
import course.QTalk.pojo.enums.ResponseCode;
import course.QTalk.util.RedisUtil;
import jodd.util.StringUtil;

public class BaseService {
    public static void verifyCheckCode(String checkCode, String sessionId, RedisUtil redisUtil) {
        if (!redisUtil.hasKey(RedisConstant.CAPTCHA_KEY + sessionId)) {
            throw new QTWebException(ResponseCode.CHECK_CODE_EXPIRED.getMessage());
        }
        String verification = (String) redisUtil.get(RedisConstant.CAPTCHA_KEY + sessionId);
        if (!StringUtil.equals(verification, checkCode)) {
            throw new QTWebException(ResponseCode.CHECK_CODE_ERROR.getMessage());
        }
        redisUtil.del(RedisConstant.CAPTCHA_KEY + sessionId);
    }
}
