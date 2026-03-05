package course.QTalk.constant;

public class RedisConstant {
    // 项目名
    public static final String PROJECT_NAME = "QTalk:";

    // 验证码
    public static final String CHECK_CODE = PROJECT_NAME + "CheckCode:";
    public static final String CAPTCHA_KEY = CHECK_CODE + "GraphicsCaptcha:";
    public static final String EMAIL_CODE = CHECK_CODE + "EmailCode:";

    // TokenKey
    public static final String TOKEN = PROJECT_NAME + "Token:";
    public static final String WEB_LOGIN = TOKEN + "WebLogin:";
    public static final String ANDROID_LOGIN = TOKEN + "AndroidLogin:";
    public static final String IOS_LOGIN = TOKEN + "IOSLogin:";

    // 幂等性Key
    public static final String IDEMPOTENT_KEY = PROJECT_NAME + "Idempotent:";

    // Netty 连接的 Channel
    public static final String NETTY_CHANNEL = PROJECT_NAME + "NettyChannel:";

    // websocket 联系人
    public static final String CONTACT_LIST = PROJECT_NAME + "Contact:";
    public static final String GROUP_LIST = CONTACT_LIST + "Groups:";
    public static final String FRIEND_LIST = CONTACT_LIST + "Friends:";
}
