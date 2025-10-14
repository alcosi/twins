package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "ProjectionExclusionCreateV1")
public class ProjectionExclusionCreateDTOv1 extends ProjectionExclusionSaveDTOv1{
}
