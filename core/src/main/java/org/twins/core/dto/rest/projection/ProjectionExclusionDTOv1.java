package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ProjectionExclusionV1")
public class ProjectionExclusionDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "twin_id")
    public UUID twinId;

    @Schema(description = "twin_class_field_id")
    public UUID twinClassFieldId;
}
