package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.enums.projection.ProjectionFieldSelector;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FieldProjectionSearchV1")
public class FieldProjectionSearchDTOv1 {
    @Schema(description = "projection field selector")
    public ProjectionFieldSelector projectionFieldSelector;

    @Schema(description = "src id list")
    public Set<UUID> srcIdList;

    @Schema(description = "dst id list")
    public Set<UUID> dstIdList;

    @Schema(description = "projection type id list")
    public Set<UUID> projectionTypeIdList;
}
