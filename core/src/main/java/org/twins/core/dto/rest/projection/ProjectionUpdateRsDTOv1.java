package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "ProjectionUpdateRsV1")
public class ProjectionUpdateRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "projection list")
    public List<ProjectionDTOv1> projectionList;
}
