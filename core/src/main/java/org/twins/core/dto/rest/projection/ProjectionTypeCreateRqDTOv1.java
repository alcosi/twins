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
@Schema(name = "ProjectionTypeCreateRqV1")
public class ProjectionTypeCreateRqDTOv1 extends Request {
    @Schema(description = "data list projections")
    public List<ProjectionTypeCreateDTOv1> projectionTypes;
}
