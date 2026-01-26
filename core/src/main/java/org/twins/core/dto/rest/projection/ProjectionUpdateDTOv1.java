package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "ProjectionUpdateV1")
public class ProjectionUpdateDTOv1 extends ProjectionSaveDTOv1 {
    @Schema(description = "projection id")
    public UUID id;
}
