package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ProjectionTypeCreateV1")
public class ProjectionTypeCreateDTOv1 {
    @NotNull
    @Schema(description = "projection type group id")
    public UUID projectionTypeGroupId;

    @NotNull
    @Schema(description = "membership twin class id")
    public UUID membershipTwinClassId;

    @NotNull
    @Schema
    public String key;

    @Schema
    public String name;
}
