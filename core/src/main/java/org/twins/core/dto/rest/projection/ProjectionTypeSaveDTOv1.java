package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ProjectionTypeSaveV1")
public class ProjectionTypeSaveDTOv1 {
    @Schema(description = "projection type group id")
    public UUID projectionTypeGroupId;

    @Schema(description = "membership twin class id")
    public UUID membershipTwinClassId;

    @Schema
    public String key;

    @Schema
    public String name;
}
