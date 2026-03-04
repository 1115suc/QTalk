package course.QTalk.pojo.enums;

public enum FriendStatus {
    // 0:正常, 1:删除, 2:拉黑
    NORMAL(0, "正常"),
    DELETED(1, "删除"),
    BLACK(2, "拉黑");

    private int code;
    private String desc;

    FriendStatus(int code, String desc) {
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
