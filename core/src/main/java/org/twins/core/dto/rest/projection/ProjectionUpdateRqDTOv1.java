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
@Schema(name =  "ProjectionUpdateRqV1")
public class ProjectionUpdateRqDTOv1 extends Request {
    @Schema(description = "projection list")
    public List<ProjectionUpdateDTOv1> projectionList;
}
