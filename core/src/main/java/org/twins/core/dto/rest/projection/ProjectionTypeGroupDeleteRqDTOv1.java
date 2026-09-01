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
@Schema(name = "ProjectionTypeGroupDeleteRqV1")
public class ProjectionTypeGroupDeleteRqDTOv1 extends Request {
    @Schema(description = "projection type group id list to delete")
    public Set<UUID> projectionTypeGroupIdList;
}
