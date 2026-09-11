package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DataListSubsetSearchV1")
public class DataListSubsetSearchDTOv1 {
    @Schema(description = "data list subset id list")
    public Set<UUID> idList;

    @Schema(description = "data list subset id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "data list id list")
    public Set<UUID> dataListIdList;

    @Schema(description = "data list id exclude list")
    public Set<UUID> dataListIdExcludeList;

    @Schema(description = "name like list")
    public Set<String> nameLikeList;

    @Schema(description = "name not like list")
    public Set<String> nameNotLikeList;

    @Schema(description = "key like list")
    public Set<String> keyLikeList;

    @Schema(description = "key not like list")
    public Set<String> keyNotLikeList;
}
