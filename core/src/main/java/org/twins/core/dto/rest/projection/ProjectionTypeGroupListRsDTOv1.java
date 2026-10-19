package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ProjectionTypeGroupListRsV1")
public class ProjectionTypeGroupListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "projection type group list")
    public List<ProjectionTypeGroupDTOv1> projectionTypeGroups;
}
