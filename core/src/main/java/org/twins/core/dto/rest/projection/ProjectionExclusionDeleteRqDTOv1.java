package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "ProjectionExclusionDeleteRqV1")
public class ProjectionExclusionDeleteRqDTOv1 extends Request {
    @Schema(description = "projection exclusion id's set")
    public Set<UUID> projectionExclusionIds;
}
