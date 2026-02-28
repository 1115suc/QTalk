package course.QTalk.pojo.enums;

public enum GroupStatus {
    // 0:正常, 1:解散, 2:封禁
    NORMAL(0, "正常"),
    DISMISSED(1, "解散"),
    BANNED(2, "封禁");

    private int code;
    private String desc;

    private GroupStatus(int code, String desc) {
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
