package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinSearchV1")
public class TwinSearchDTOv1 {
    @Schema(description = "Twin class id list")
    public List<UUID> twinClassIdList;

    @Schema(description = "Twin class exclude list")
    public List<UUID> twinClassIdExcludeList;

    @Schema(description = "Twin name like list")
    public List<String> twinNameLikeList;

    @Schema(description = "Head twin id list")
    public List<UUID> headTwinIdList;

    @Schema(description = "Twin id list")
    public List<UUID> twinIdList;

    @Schema(description = "Twin id exclude list")
    public List<UUID> twinIdExcludeList;

    @Schema(description = "Status id list")
    public List<UUID> statusIdList;

    @Schema(description = "Assigner id list")
    public List<UUID> assignerUserIdList;

    @Schema(description = "Reporter id list")
    public List<UUID> createdByUserIdList;

    @Schema(description = "Include dst twins with given links")
    public List<TwinSearchByLinkDTOv1> linksList;

    @Schema(description = "Exclude dst twins with given links")
    public List<TwinSearchByLinkDTOv1> noLinksList;

    @Schema(description = "Hierarchy ids filter")
    public List<UUID>  hierarchyTreeContainsIdList;

    @Schema(description = "Twin status exclude list")
    public List<UUID>  statusIdExcludeList;

    @Schema(description = "Twin tag list(data list options ids)")
    public List<UUID> tagDataListOptionIdList;

    @Schema(description = "Twin tag exclude list(data list options ids)")
    public List<UUID> tagDataListOptionIdExcludeList;

    @Schema(description = "Twin marker list(data list options ids)")
    public List<UUID> markerDataListOptionIdList;

    @Schema(description = "Twin marker exclude list(data list options ids)")
    public List<UUID> markerDataListOptionIdExcludeList;

}
