package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ProjectionTypeGroupCreateRqV1")
public class ProjectionTypeGroupCreateRqDTOv1 extends Request {
    @Valid
    @Schema(description = "projection type groups")
    public List<ProjectionTypeGroupCreateDTOv1> projectionTypeGroups;
}
