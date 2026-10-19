package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinLinkSearchDTOv1")
public class TwinLinkSearchDTOv1 {
    @Size(max = 50)
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Size(max = 50)
    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Size(max = 50)
    @Schema(description = "source twin id list")
    public Set<UUID> srcTwinIdList;

    @Size(max = 50)
    @Schema(description = "source twin id exclude list")
    public Set<UUID> srcTwinIdExcludeList;

    @Size(max = 50)
    @Schema(description = "destination twin id list")
    public Set<UUID> dstTwinIdList;

    @Size(max = 50)
    @Schema(description = "destination twin id exclude list")
    public Set<UUID> dstTwinIdExcludeList;

    @Size(max = 50)
    @Schema(description = "source or destination twin id list")
    public Set<UUID> srcOrDstTwinIdList;

    @Size(max = 50)
    @Schema(description = "source or destination twin id exclude list")
    public Set<UUID> srcOrDstTwinIdExcludeList;

    @Size(max = 50)
    @Schema(description = "link id list")
    public Set<UUID> linkIdList;

    @Size(max = 50)
    @Schema(description = "link id exclude list")
    public Set<UUID> linkIdExcludeList;

    @Size(max = 50)
    @Schema(description = "created by user id list")
    public Set<UUID> createdByUserIdList;

    @Size(max = 50)
    @Schema(description = "created by user id exclude list")
    public Set<UUID> createdByUserIdExcludeList;

    @Schema(description = "createdAt range")
    public DataTimeRangeDTOv1 createdAt;
}
