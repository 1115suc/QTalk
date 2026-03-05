package course.QTalk.pojo.enums;

import cn.hutool.core.util.StrUtil;

public enum ContactType {
    // 1:用户 2:群组
    USER(1, "U", "用户"),
    GROUP(2, "Q", "群组");

    private Integer code;
    private String prefix;
    private String message;

    ContactType(Integer code, String prefix, String message) {
        this.code = code;
        this.prefix = prefix;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getPrefix() {
        return prefix;
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

    public static ContactType getByPrefix(String prefix) {
        try {
            if(StrUtil.isBlank(prefix) || prefix.trim().length() == 0) {
                return null;
            }
            prefix = prefix.substring(0,1);
            prefix = prefix.toUpperCase();
            for (ContactType value : ContactType.values()){
                if(value.getPrefix().equals(prefix)){
                    return value;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
