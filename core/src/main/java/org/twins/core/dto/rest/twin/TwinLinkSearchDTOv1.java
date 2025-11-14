package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinLinkSearchV1")
public class TwinLinkSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "src twin id list")
    public Set<UUID> srcTwinIdList;

    @Schema(description = "src twin id exclude list")
    public Set<UUID> srcTwinIdExcludeList;

    @Schema(description = "dst twin id list")
    public Set<UUID> dstTwinIdList;

    @Schema(description = "dst twin id exclude list")
    public Set<UUID> dstTwinIdExcludeList;

    @Schema(description = " link id list")
    public Set<UUID> linkIdList;

    @Schema(description = " link id exclude list")
    public Set<UUID> linkIdExcludeList;
}
