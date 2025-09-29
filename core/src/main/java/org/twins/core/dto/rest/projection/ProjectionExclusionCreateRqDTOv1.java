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
@Schema(name =  "ProjectionExclusionCreateRqV1")
public class ProjectionExclusionCreateRqDTOv1 extends Request {
    @Schema(description = "projection exclusion list")
    public List<ProjectionExclusionCreateDTOv1> projectionList;
}
