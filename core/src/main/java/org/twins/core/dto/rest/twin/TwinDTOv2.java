package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentViewDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinV2")
public class TwinDTOv2 {
    @Schema(description = "id", example = DTOExamples.TWIN_ID)
    public UUID id;

    @Schema(description = "externalId", example = "934599502DFFAE")
    public String externalId;

    @Schema(description = "created at", example = "1549632759")
    public Instant createdAt;

    @Schema(description = "name", example = "Oak")
    public String name;

    @Schema(description = "description", example = "The biggest tree")
    public String description;

    @Schema(description = "status")
    public TwinStatusDTOv1 status;

    @Schema(description = "class")
    public TwinClassDTOv1 twinClass;

    @Schema(description = "current assigner")
    public UserDTOv1 assignerUser;

    @Schema(description = "author")
    public UserDTOv1 authorUser;

    @Schema(description = "fields")
    public Map<String, String> fields;

    @Schema(description = "attachments")
    public List<AttachmentViewDTOv1> attachments;
}