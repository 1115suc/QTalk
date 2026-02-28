package course.QTalk.pojo.enums;

public enum GroupRole {
    NORMAL(1, "普通成员"),
    ADMIN(2, "管理员"),
    OWNER(3, "群主");

    private int code;
    private String desc;

    private GroupRole(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
