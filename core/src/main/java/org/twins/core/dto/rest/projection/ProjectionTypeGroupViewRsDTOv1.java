package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ProjectionTypeGroupViewRsV1")
public class ProjectionTypeGroupViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - projection type group")
    public ProjectionTypeGroupDTOv1 projectionTypeGroup;
}
