package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;
import org.twins.core.enums.twin.Touch;

import java.util.*;

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
    public List<Touch> touchList;

    @Schema(description = "Twin touch exclude list ids")
    public List<Touch> touchExcludeList;

    @Schema(description = "Twin Field Search. Key TwinClassField id.", type = "object", additionalPropertiesSchema = TwinFieldSearchDTOv1.class, example = """
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
            """)
    public Map<UUID, TwinFieldSearchDTOv1> fields;

    @Schema(description = "created at")
    public DataTimeRangeDTOv1 createdAt;

    @Schema(description = "Set of hierarchy paths")
    public Set<String> hierarchyPathSet;

    @Schema(description = "Max twin children depth")
    public Integer maxChildrenDepth;


    public TwinSearchDTOv1 addTwinClassIdListItem(UUID item) {
        this.twinClassIdList = CollectionUtils.safeAdd(this.twinClassIdList, item);
        return this;
    }

    public TwinSearchDTOv1 addTwinClassIdExcludeListItem(UUID item) {
        this.twinClassIdExcludeList = CollectionUtils.safeAdd(this.twinClassIdExcludeList, item);
        return this;
    }

    public TwinSearchDTOv1 addTwinNameLikeListItem(String item) {
        this.twinNameLikeList = CollectionUtils.safeAdd(this.twinNameLikeList, item);
        return this;
    }

    public TwinSearchDTOv1 addTwinNameNotLikeListItem(String item) {
        this.twinNameNotLikeList = CollectionUtils.safeAdd(this.twinNameNotLikeList, item);
        return this;
    }

    public TwinSearchDTOv1 addDescriptionLikeListItem(String item) {
        this.descriptionLikeList = CollectionUtils.safeAdd(this.descriptionLikeList, item);
        return this;
    }

    public TwinSearchDTOv1 addDescriptionNotLikeListItem(String item) {
        this.descriptionNotLikeList = CollectionUtils.safeAdd(this.descriptionNotLikeList, item);
        return this;
    }

    public TwinSearchDTOv1 addHeadTwinIdListItem(UUID item) {
        this.headTwinIdList = CollectionUtils.safeAdd(this.headTwinIdList, item);
        return this;
    }

    public TwinSearchDTOv1 addTwinIdListItem(UUID item) {
        this.twinIdList = CollectionUtils.safeAdd(this.twinIdList, item);
        return this;
    }

    public TwinSearchDTOv1 addTwinIdExcludeListItem(UUID item) {
        this.twinIdExcludeList = CollectionUtils.safeAdd(this.twinIdExcludeList, item);
        return this;
    }

    public TwinSearchDTOv1 addStatusIdListItem(UUID item) {
        this.statusIdList = CollectionUtils.safeAdd(this.statusIdList, item);
        return this;
    }

    public TwinSearchDTOv1 addAssignerUserIdListItem(UUID item) {
        this.assignerUserIdList = CollectionUtils.safeAdd(this.assignerUserIdList, item);
        return this;
    }

    public TwinSearchDTOv1 addAssignerUserIdExcludeListItem(UUID item) {
        this.assignerUserIdExcludeList = CollectionUtils.safeAdd(this.assignerUserIdExcludeList, item);
        return this;
    }

    public TwinSearchDTOv1 addCreatedByUserIdListItem(UUID item) {
        this.createdByUserIdList = CollectionUtils.safeAdd(this.createdByUserIdList, item);
        return this;
    }

    public TwinSearchDTOv1 addCreatedByUserIdExcludeListItem(UUID item) {
        this.createdByUserIdExcludeList = CollectionUtils.safeAdd(this.createdByUserIdExcludeList, item);
        return this;
    }

    public TwinSearchDTOv1 addExternalIdListItem(String item) {
        this.externalIdList = CollectionUtils.safeAdd(this.externalIdList, item);
        return this;
    }

    public TwinSearchDTOv1 addExternalIdExcludeListItem(String item) {
        this.externalIdExcludeList = CollectionUtils.safeAdd(this.externalIdExcludeList, item);
        return this;
    }

    public TwinSearchDTOv1 addLinksAnyOfListItem(TwinSearchByLinkDTOv1 item) {
        this.linksAnyOfList = CollectionUtils.safeAdd(this.linksAnyOfList, item);
        return this;
    }

    public TwinSearchDTOv1 addLinksNoAnyOfListItem(TwinSearchByLinkDTOv1 item) {
        this.linksNoAnyOfList = CollectionUtils.safeAdd(this.linksNoAnyOfList, item);
        return this;
    }

    public TwinSearchDTOv1 addLinksAllOfListItem(TwinSearchByLinkDTOv1 item) {
        this.linksAllOfList = CollectionUtils.safeAdd(this.linksAllOfList, item);
        return this;
    }

    public TwinSearchDTOv1 addLinksNoAllOfListItem(TwinSearchByLinkDTOv1 item) {
        this.linksNoAllOfList = CollectionUtils.safeAdd(this.linksNoAllOfList, item);
        return this;
    }

    public TwinSearchDTOv1 addHierarchyTreeContainsIdListItem(UUID item) {
        this.hierarchyTreeContainsIdList = CollectionUtils.safeAdd(this.hierarchyTreeContainsIdList, item);
        return this;
    }

    public TwinSearchDTOv1 addStatusIdExcludeListItem(UUID item) {
        this.statusIdExcludeList = CollectionUtils.safeAdd(this.statusIdExcludeList, item);
        return this;
    }

    public TwinSearchDTOv1 addTagDataListOptionIdListItem(UUID item) {
        this.tagDataListOptionIdList = CollectionUtils.safeAdd(this.tagDataListOptionIdList, item);
        return this;
    }

    public TwinSearchDTOv1 addTagDataListOptionIdExcludeListItem(UUID item) {
        this.tagDataListOptionIdExcludeList = CollectionUtils.safeAdd(this.tagDataListOptionIdExcludeList, item);
        return this;
    }

    public TwinSearchDTOv1 addMarkerDataListOptionIdListItem(UUID item) {
        this.markerDataListOptionIdList = CollectionUtils.safeAdd(this.markerDataListOptionIdList, item);
        return this;
    }

    public TwinSearchDTOv1 addMarkerDataListOptionIdExcludeListItem(UUID item) {
        this.markerDataListOptionIdExcludeList = CollectionUtils.safeAdd(this.markerDataListOptionIdExcludeList, item);
        return this;
    }

    public TwinSearchDTOv1 addTwinClassExtendsHierarchyContainsIdListItem(UUID item) {
        this.twinClassExtendsHierarchyContainsIdList = CollectionUtils.safeAdd(this.twinClassExtendsHierarchyContainsIdList, item);
        return this;
    }

    public TwinSearchDTOv1 addHeadTwinClassIdListItem(UUID item) {
        this.headTwinClassIdList = CollectionUtils.safeAdd(this.headTwinClassIdList, item);
        return this;
    }

    public TwinSearchDTOv1 addTouchListItem(Touch item) {
        this.touchList = CollectionUtils.safeAdd(this.touchList, item);
        return this;
    }

    public TwinSearchDTOv1 addTouchExcludeListItem(Touch item) {
        this.touchExcludeList = CollectionUtils.safeAdd(this.touchExcludeList, item);
        return this;
    }

    public TwinSearchDTOv1 putFieldsItem(UUID key, TwinFieldSearchDTOv1 item) {
        if (this.fields == null) this.fields = new HashMap<>();
        this.fields.put(key, item);
        return this;
    }

}
