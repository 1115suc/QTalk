import cn.hutool.json.JSONUtil;
import course.QTalk.QTalkBackendApplication;
import course.QTalk.constant.MinIOConstant;
import course.QTalk.minio.model.FileUploadResponse;
import course.QTalk.minio.service.MinIOFileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Name;
import java.io.*;

@SpringBootTest(classes = QTalkBackendApplication.class)
public class MinIOTest {
    @Autowired
    private MinIOFileService minIOFileService;

    @Test
    public void testUploadImage() throws IOException {
        File file = new File("D:\\Develop\\JavaProject\\QTalk\\document\\img\\1d38f66f97e308c9.jpg");
        InputStream inputStream = new FileInputStream(file);

        MultipartFile avatar = new MockMultipartFile(
                "avatar",           // 参数名
                file.getName(),      // 文件名
                "image/jpeg",       // 内容类型
                inputStream          // 文件内容
        );
        try {
            String name = "group/avatar/2026/02/28/1d38f66f97e308c9.jpg";
            // FileUploadResponse fileUploadResponse = minIOFileService.uploadImage(avatar, MinIOConstant.BUCKET_NAME, MinIOConstant.GROUP_AVATAR_DIR, false);
            // System.out.println(JSONUtil.toJsonStr(fileUploadResponse));
            String previewUrl = minIOFileService.getPreviewUrl(MinIOConstant.BUCKET_NAME, name);
            System.out.println(previewUrl);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
