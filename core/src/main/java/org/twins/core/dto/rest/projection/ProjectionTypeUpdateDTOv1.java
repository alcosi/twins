package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "ProjectionTypeUpdateV1")
public class ProjectionTypeUpdateDTOv1 extends ProjectionTypeSaveDTOv1 {
    @Schema
    public UUID id;
}
