package course.QTalk.util;

import cn.hutool.core.util.StrUtil;
import course.QTalk.pojo.dto.TokenUserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParseUtil {

    private final RedisComponent redisComponent;

    // LoginType 和 Authorization 作为请求 query 传入的解析方法
    private TokenUserDTO parseUrl(String url) {
        if (StrUtil.isEmpty(url) || url.indexOf("?") == -1) {
            return null;
        }

        String[] queryParams = url.split("\\?", 2);
        if (queryParams.length != 2) {
            return null;
        }

        // 解析查询参数字符串：LoginType=1&Authorization=xxx
        String queryString = queryParams[1];
        String[] params = queryString.split("&");

        String loginTypeValue = null;
        String authorizationValue = null;

        for (String param : params) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                if ("LoginType".equals(keyValue[0])) {
                    loginTypeValue = keyValue[1];
                } else if ("Authorization".equals(keyValue[0])) {
                    authorizationValue = keyValue[1];
                }
            }
        }

        if (loginTypeValue == null || authorizationValue == null) {
            return null;
        }
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(loginTypeValue, authorizationValue);

        return tokenUserDTO;
    }
}
