package course.QTalk.aspect;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import course.QTalk.annotation.VerificationInterceptor;
import course.QTalk.pojo.enums.LoginTypeEnum;
import course.QTalk.exception.QTException;
import course.QTalk.exception.QTWebException;
import course.QTalk.pojo.enums.ResponseCode;
import course.QTalk.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component("verificationOptionalAspect")
@RequiredArgsConstructor
public class VerificationOptionalAspect {

    private final RedisUtil redisUtil;

    @Before("@annotation(course.QTalk.annotation.VerificationInterceptor)")
    public void interceptorDo(JoinPoint point) {
        try{
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            VerificationInterceptor interceptor = method.getAnnotation(VerificationInterceptor.class);
            String message = interceptor.message();
            if (interceptor == null) {
                return;
            }
            if (interceptor.checkLogin()) {
                checkLogin();
            }
        }catch (QTWebException e){
            throw e;
        }catch (Exception e) {
            log.error("全局拦截器异常" ,e);
            throw new QTException(ResponseCode.SERVER_ERROR.getMessage(),ResponseCode.SERVER_ERROR.getCode());
        }catch (Throwable e){
            log.error("全局拦截器异常" ,e);
            throw new QTException(ResponseCode.SERVER_ERROR.getMessage(),ResponseCode.SERVER_ERROR.getCode());
        }
    }

    private void checkLogin() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("Authorization");
        String type = request.getHeader("LoginType");
        if (StrUtil.isBlank(type)) {
            throw new QTWebException(ResponseCode.LOGIN_FROM_ERROR.getMessage(), ResponseCode.LOGIN_FROM_ERROR.getCode());
        }
        Integer loginType = Convert.toInt(type);
        String redisPrefix = LoginTypeEnum.of(loginType).getPrefix();
        boolean hasKey = redisUtil.hasKey(redisPrefix + token);
        if (!hasKey) {
            throw new QTWebException(ResponseCode.LOGIN_TIMEOUT.getMessage(), ResponseCode.LOGIN_TIMEOUT.getCode());
        }
    }
}
