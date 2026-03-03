package course.QTalk.pojo.enums;

public enum ApplyStatus {
    PENDING(0, "待处理"),
    AGREE(1, "已同意"),
    REJECT(2, "已拒绝"),
    IGNORE(3, "已忽略");

    private Integer code;
    private String message;

    ApplyStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}