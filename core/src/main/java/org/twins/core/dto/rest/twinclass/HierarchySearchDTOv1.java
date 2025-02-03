package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.UUID;

public class HierarchySearchDTOv1 extends Request {
    @Schema(description = "twin class id list")
    public List<UUID> headHierarchyChildsForTwinClassIdList;
}
