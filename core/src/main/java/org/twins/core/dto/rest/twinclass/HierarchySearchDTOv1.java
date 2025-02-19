package org.twins.core.dto.rest.twinclass;

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
@Schema(name = "TwinClassListRqV1")
public class HierarchySearchDTOv1 extends Request {
    @Schema(description = "twin class id list")
    public Set<UUID> twinClassIdList;
    @Schema(description = "twin class id exclude list")
    public Set<UUID> twinClassIdExcludeList;
    @Schema(description = "Search depth")
    public Integer depth = 1;
}
