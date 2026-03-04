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

    public static ApplyStatus getByCode(Integer code) {
        for (ApplyStatus applyStatus : ApplyStatus.values()) {
            if (applyStatus.code.equals(code)) {
                return applyStatus;
            }
        }
        return null;
    }
}