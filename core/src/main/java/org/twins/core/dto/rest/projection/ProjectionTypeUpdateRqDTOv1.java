package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ProjectionTypeUpdateRqV1")
public class ProjectionTypeUpdateRqDTOv1 extends Request {
    @Schema(description = "Projection type list")
    public List<ProjectionTypeUpdateDTOv1> projectionTypes;
}
