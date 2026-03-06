package course.QTalk.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;

import java.util.Arrays;

public class ToolUtils {

    public static String cleanHtmlTag(String content) {
        if (StrUtil.isBlank(content)) {
            return content;
        }

        // 转义 < 符号，防止 XSS 或 HTML 注入
        content = content.replace("<", "&lt;");
        // 替换 \r\n 为 <br> 标签
        content = content.replace("\r\n", "<br>");
        // 替换 "An"（疑似误写，若意图是替换换行符，请根据实际需求调整）
        content = content.replace("\n", "<br>");

        return content;
    }

    public static String getUserChatSession(String userId, String friendId) {
        String[] ids = {userId, friendId};
        Arrays.sort(ids);
        String session = DigestUtil.md5Hex(ids[0] + ids[1]);
        return session.substring(0, 32);
    }

    public static String getGroupChatSession(String groupId) {
        String session = DigestUtil.md5Hex(groupId);
        return session.substring(0, 32);
    }
}
