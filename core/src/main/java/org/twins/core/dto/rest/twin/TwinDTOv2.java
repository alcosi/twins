package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.attachment.AttachmentViewDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "TwinV2")
public class TwinDTOv2 extends TwinBaseDTOv1 {
    @Schema(description = "statusId")
    public UUID statusId;

    @Schema(description = "status")
    public TwinStatusDTOv1 status;

    @Schema(description = "class")
    public UUID twinClassId;

    @Schema(description = "class")
    public TwinClassDTOv1 twinClass;

    @Schema(description = "current assigner")
    public UUID assignerUserId;

    @Schema(description = "current assigner")
    public UserDTOv1 assignerUser;

    @Schema(description = "author")
    public UUID authorUserId;

    @Schema(description = "author")
    public UserDTOv1 authorUser;

    @Schema(description = "fields")
    public Map<String, String> fields;

    @Schema(description = "attachments")
    public List<AttachmentViewDTOv1> attachments;
}
