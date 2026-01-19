package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "HierarchySearchV1")
public class HierarchySearchDTOv1  {
    @Schema(description = "twin class id list")
    public Set<UUID> idList;

    @Schema(description = "twin class id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "hierarchy list")
    public Set<String> hierarchyList;

    @Schema(description = "Search depth")
    public Integer depth = 1;
}
