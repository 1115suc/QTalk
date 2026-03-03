package course.QTalk.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "群基础信息")
public class GroupBasicInfoVO {
    @Schema(description = "群组ID")
    private String groupId;

    @Schema(description = "群组名称")
    private String name;
}
