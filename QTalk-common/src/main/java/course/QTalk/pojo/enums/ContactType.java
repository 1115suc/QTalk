package course.QTalk.pojo.enums;

public enum ContactType {
    // 1:用户 2:群组
    USER(1, "用户"),
    GROUP(2, "群组");

    private Integer code;
    private String message;

    ContactType(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ContactType getByCode(Integer code) {
        for (ContactType value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
