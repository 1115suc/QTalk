package course.QTalk.pojo.enums;

public enum ResponseCodeEnum {
    CODE_200(200, "请求成功"),
    CODE_404(404, "请求地址不存在"),
    CODE_429(429, "请求过于频繁，请稍后重试"),
    CODE_600(600, "请求参数错误"),
    CODE_601(601, "信息已经存在"),
    CODE_602(602, "文件不存在"),
    CODE_901(901, "登录超时"),
    CODE_902(902, "您不是对方的好友，请先添加对方"),
    CODE_903(903, "您不在群聊中，请重新添加群聊"),
    CODE_500(500, "服务器返回错误，请联系管理员");

    private Integer code;

    private String msg;

    ResponseCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}