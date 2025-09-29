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
@Schema(name = "ProjectionExclusionCreateRsV1")
public class ProjectionExclusionCreateRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "projection exclusion list")
    public List<ProjectionExclusionDTOv1> projectionList;
}
