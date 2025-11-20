package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "DataListProjectionSearchV1")
public class DataListProjectionSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "src data list id list")
    public Set<UUID> srcDataListIdList;

    @Schema(description = "src data list id exclude list")
    public Set<UUID> srcDataListIdExcludeList;

    @Schema(description = "dst data list id list")
    public Set<UUID> dstDataListIdList;

    @Schema(description = "dst data list id exclude list")
    public Set<UUID> dstDataListIdExcludeList;

    @Schema(description = "datalist name like list")
    public Set<String> nameLikeList;

    @Schema(description = "datalist name not like list")
    public Set<String> nameNotLikeList;

    @Schema(description = "saved by user id list")
    public Set<UUID> savedByUserIdList;

    @Schema(description = "saved by user id exclude list")
    public Set<UUID> savedByUserIdExcludeList;

    @Schema(description = "changed at")
    public DataTimeRangeDTOv1 changedAt;
}
