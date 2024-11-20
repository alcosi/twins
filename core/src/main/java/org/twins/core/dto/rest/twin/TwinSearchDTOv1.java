package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinTouchEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinSearchV1")
public class TwinSearchDTOv1 {
    @Schema(description = "Twin class id list")
    public List<UUID> twinClassIdList;

    @Schema(description = "Twin class id exclude list")
    public List<UUID> twinClassIdExcludeList;

    @Schema(description = "Twin name like list")
    public List<String> twinNameLikeList;

    @Schema(description = "Twin name not like list")
    public List<String> twinNameNotLikeList;

    @Schema(description = "Twin description like list")
    public List<String> descriptionLikeList;

    @Schema(description = "Twin description not like list")
    public List<String> descriptionNotLikeList;

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

    @Schema(description = "Assigner id exclude list")
    public List<UUID> assignerUserIdExcludeList;

    @Schema(description = "Reporter id list")
    public List<UUID> createdByUserIdList;

    @Schema(description = "Reporter id exclude list")
    public List<UUID> createdByUserIdExcludeList;

    @Schema(description = "Include dst twins with given links. OR join")
    public List<TwinSearchByLinkDTOv1> linksAnyOfList;

    @Schema(description = "Exclude dst twins with given links. OR join")
    public List<TwinSearchByLinkDTOv1> linksNoAnyOfList;

    @Schema(description = "Include dst twins with given links. AND join")
    public List<TwinSearchByLinkDTOv1> linksAllOfList;

    @Schema(description = "Exclude dst twins with given links. AND join")
    public List<TwinSearchByLinkDTOv1> linksNoAllOfList;

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

    @Schema(description = "Twin extends by twin class list ids")
    public List<UUID> extendsTwinClassIdList;

    @Schema(description = "Head twin class list ids")
    public List<UUID> headTwinClassIdList;

    @Schema(description = "Twin touch list ids")
    public List<TwinTouchEntity.Touch> touchList;

    @Schema(description = "Twin touch exclude list ids")
    public List<TwinTouchEntity.Touch> touchExcludeList;

    @Schema(description = "Twin Field Search. Key TwinClassField id.")
    public Map<UUID, TwinFieldSearchDTOv1> fields;

}
