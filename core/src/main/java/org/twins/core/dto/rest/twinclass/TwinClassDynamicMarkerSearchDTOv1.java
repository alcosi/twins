package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinClassDynamicMarkerSearchV1")
public class TwinClassDynamicMarkerSearchDTOv1 {

    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twin class id list")
    public Set<UUID> twinClassIdList;

    @Schema(description = "twin class id exclude list")
    public Set<UUID> twinClassIdExcludeList;

    @Schema(description = "twin validator set id list")
    public Set<UUID> twinValidatorSetIdList;

    @Schema(description = "twin validator set id exclude list")
    public Set<UUID> twinValidatorSetIdExcludeList;

    @Schema(description = "marker data list option id list")
    public Set<UUID> markerDataListOptionIdList;

    @Schema(description = "marker data list option id exclude list")
    public Set<UUID> markerDataListOptionIdExcludeList;
}
