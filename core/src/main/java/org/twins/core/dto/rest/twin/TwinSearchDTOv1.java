package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinTouchEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinSearchV1")
public class TwinSearchDTOv1 {
    @Schema(description = "Twin class id list")
    public Set<UUID> twinClassIdList;

    @Schema(description = "Twin class id exclude list")
    public Set<UUID> twinClassIdExcludeList;

    @Schema(description = "Twin name like list")
    public Set<String> twinNameLikeList;

    @Schema(description = "Twin name not like list")
    public Set<String> twinNameNotLikeList;

    @Schema(description = "Twin description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "Twin description not like list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "Head twin id list")
    public Set<UUID> headTwinIdList;

    @Schema(description = "Twin id list")
    public Set<UUID> twinIdList;

    @Schema(description = "Twin id exclude list")
    public Set<UUID> twinIdExcludeList;

    @Schema(description = "Status id list")
    public Set<UUID> statusIdList;

    @Schema(description = "Assigner id list")
    public Set<UUID> assignerUserIdList;

    @Schema(description = "Assigner id exclude list")
    public Set<UUID> assignerUserIdExcludeList;

    @Schema(description = "Reporter id list")
    public Set<UUID> createdByUserIdList;

    @Schema(description = "Reporter id exclude list")
    public Set<UUID> createdByUserIdExcludeList;

    @Schema(description = "External id list")
    public Set<String> externalIdList;

    @Schema(description = "External id exclude list")
    public Set<String> externalIdExcludeList;

    @Schema(description = "Include dst twins with given links. OR join")
    public List<TwinSearchByLinkDTOv1> linksAnyOfList;

    @Schema(description = "Exclude dst twins with given links. OR join")
    public List<TwinSearchByLinkDTOv1> linksNoAnyOfList;

    @Schema(description = "Include dst twins with given links. AND join")
    public List<TwinSearchByLinkDTOv1> linksAllOfList;

    @Schema(description = "Exclude dst twins with given links. AND join")
    public List<TwinSearchByLinkDTOv1> linksNoAllOfList;

    @Schema(description = "Hierarchy ids filter")
    public Set<UUID> hierarchyTreeContainsIdList;

    @Schema(description = "Twin status exclude list")
    public Set<UUID> statusIdExcludeList;

    @Schema(description = "Twin tag list(data list options ids)")
    public Set<UUID> tagDataListOptionIdList;

    @Schema(description = "Twin tag exclude list(data list options ids)")
    public Set<UUID> tagDataListOptionIdExcludeList;

    @Schema(description = "Twin marker list(data list options ids)")
    public Set<UUID> markerDataListOptionIdList;

    @Schema(description = "Twin marker exclude list(data list options ids)")
    public Set<UUID> markerDataListOptionIdExcludeList;

    @Schema(description = "Twin extends by twin class list ids")
    public Set<UUID> twinClassExtendsHierarchyContainsIdList;

    @Schema(description = "Head twin class list ids")
    public Set<UUID> headTwinClassIdList;

    @Schema(description = "Twin touch list ids")
    public List<TwinTouchEntity.Touch> touchList;

    @Schema(description = "Twin touch exclude list ids")
    public List<TwinTouchEntity.Touch> touchExcludeList;

    @Schema(description = "Twin Field Search. Key TwinClassField id.", type = "object", additionalPropertiesSchema = TwinFieldSearchDTOv1.class,
            example = """
                    {
                        "550e8400-e29b-41d4-a716-446655440000": {
                            "type": "TwinFieldSearchNumericV1",
                            "lessThen": "10",
                            "moreThen": "5",
                            "equals": "7"
                        },
                        "550e8400-e29b-41d4-a716-446655440001": {
                            "type": "TwinFieldSearchTextV1",
                            "valueLikeAllOfList": ["test%"]
                        }
                    }
                    """
    )
    public Map<UUID, TwinFieldSearchDTOv1> fields;

}
