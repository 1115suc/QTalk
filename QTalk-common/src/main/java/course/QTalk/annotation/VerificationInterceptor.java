package course.QTalk.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VerificationInterceptor {
    boolean checkLogin() default true;

    String message() default "";
}
