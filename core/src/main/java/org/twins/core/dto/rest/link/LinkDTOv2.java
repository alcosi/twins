package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassLinkV2")
public class LinkDTOv2 extends LinkDTOv1 {

    @Schema(description = "Source twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID srcTwinClassId;
    @Schema(description = "Backward name", example = "dst -> src")
    public String backwardName;
    @Schema(description = "Creator user id", example = DTOExamples.USER_ID)
    public UUID createdByUserId;
    @Schema(description = "Creation timestamp")
    public LocalDateTime createdAt;

}
