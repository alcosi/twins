package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "ProjectionExclusionSearchV1")
public class ProjectionExclusionSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twin id list")
    public Set<UUID> twinIdList;

    @Schema(description = "twin id exclude list")
    public Set<UUID> twinIdExcludeList;

    @Schema(description = "twin class field id list")
    public Set<UUID> twinClassFieldIdList;

    @Schema(description = "twin class field id exclude list")
    public Set<UUID> twinClassFieldIdExcludeList;
}
