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
@Schema(name =  "ProjectionCreateRqV1")
public class ProjectionCreateRqDTOv1 extends Request {
    @Schema(description = "projection list")
    public List<ProjectionCreateDTOv1> projectionList;
}
