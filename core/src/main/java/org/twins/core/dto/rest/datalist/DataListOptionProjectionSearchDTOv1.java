package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "DataListOptionProjectionSearchV1")
public class DataListOptionProjectionSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "projection type id list")
    public Set<UUID> projectionTypeIdList;

    @Schema(description = "projection type id exclude list")
    public Set<UUID> projectionTypeIdExcludeList;

    @Schema(description = "scr data list option id list")
    public Set<UUID> srcDataListOptionIdList;

    @Schema(description = "scr data list option id exclude list")
    public Set<UUID> srcDataListOptionIdExcludeList;

    @Schema(description = "dst data list option id list")
    public Set<UUID> dstDataListOptionIdList;

    @Schema(description = "dst data list option id exclude list")
    public Set<UUID> dstDataListOptionIdExcludeList;

    @Schema(description = "saved by user id list")
    public Set<UUID> savedByUserIdList;

    @Schema(description = "saved by user id exclude list")
    public Set<UUID> savedByUserIdExcludeList;

    @Schema(description = "changed at")
    public DataTimeRangeDTOv1 changedAt;
}
