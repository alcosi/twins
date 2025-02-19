package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

public class HierarchySearchDTOv1 extends Request {
    @Schema(description = "twin class id list")
    public Set<UUID> twinClassIdList;
    @Schema(description = "twin class id exclude list")
    public Set<UUID> twinClassIdExcludeList;
    public Integer depth = 1;
}
