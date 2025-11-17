package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "ProjectionExclusionSaveV1")
public class ProjectionExclusionSaveDTOv1 {
    @Schema(description = "twin id")
    public UUID twinId;

    @Schema(description = "twin class field id")
    public UUID twinClassFieldId;
}
