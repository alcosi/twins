package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "ProjectionTypeV1")
public class ProjectionTypeGroupDTOv1 {
    @Schema
    public UUID id;

    @Schema
    public String key;
}
