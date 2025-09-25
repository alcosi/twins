package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.enums.twin.Touch;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinSearchRqV1")
public class TwinSearchRqDTOv1 extends TwinSearchExtendedDTOv1 {


    @Override
    public TwinSearchRqDTOv1 addTwinClassIdListItem(UUID item) {
        this.twinClassIdList = CollectionUtils.safeAdd(this.twinClassIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTwinClassIdExcludeListItem(UUID item) {
        this.twinClassIdExcludeList = CollectionUtils.safeAdd(this.twinClassIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTwinNameLikeListItem(String item) {
        this.twinNameLikeList = CollectionUtils.safeAdd(this.twinNameLikeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTwinNameNotLikeListItem(String item) {
        this.twinNameNotLikeList = CollectionUtils.safeAdd(this.twinNameNotLikeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addDescriptionLikeListItem(String item) {
        this.descriptionLikeList = CollectionUtils.safeAdd(this.descriptionLikeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addDescriptionNotLikeListItem(String item) {
        this.descriptionNotLikeList = CollectionUtils.safeAdd(this.descriptionNotLikeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addHeadTwinIdListItem(UUID item) {
        this.headTwinIdList = CollectionUtils.safeAdd(this.headTwinIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTwinIdListItem(UUID item) {
        this.twinIdList = CollectionUtils.safeAdd(this.twinIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTwinIdExcludeListItem(UUID item) {
        this.twinIdExcludeList = CollectionUtils.safeAdd(this.twinIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addStatusIdListItem(UUID item) {
        this.statusIdList = CollectionUtils.safeAdd(this.statusIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addAssignerUserIdListItem(UUID item) {
        this.assignerUserIdList = CollectionUtils.safeAdd(this.assignerUserIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addAssignerUserIdExcludeListItem(UUID item) {
        this.assignerUserIdExcludeList = CollectionUtils.safeAdd(this.assignerUserIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addCreatedByUserIdListItem(UUID item) {
        this.createdByUserIdList = CollectionUtils.safeAdd(this.createdByUserIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addCreatedByUserIdExcludeListItem(UUID item) {
        this.createdByUserIdExcludeList = CollectionUtils.safeAdd(this.createdByUserIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addExternalIdListItem(String item) {
        this.externalIdList = CollectionUtils.safeAdd(this.externalIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addExternalIdExcludeListItem(String item) {
        this.externalIdExcludeList = CollectionUtils.safeAdd(this.externalIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addLinksAnyOfListItem(TwinSearchByLinkDTOv1 item) {
        this.linksAnyOfList = CollectionUtils.safeAdd(this.linksAnyOfList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addLinksNoAnyOfListItem(TwinSearchByLinkDTOv1 item) {
        this.linksNoAnyOfList = CollectionUtils.safeAdd(this.linksNoAnyOfList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addLinksAllOfListItem(TwinSearchByLinkDTOv1 item) {
        this.linksAllOfList = CollectionUtils.safeAdd(this.linksAllOfList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addLinksNoAllOfListItem(TwinSearchByLinkDTOv1 item) {
        this.linksNoAllOfList = CollectionUtils.safeAdd(this.linksNoAllOfList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addHierarchyTreeContainsIdListItem(UUID item) {
        this.hierarchyTreeContainsIdList = CollectionUtils.safeAdd(this.hierarchyTreeContainsIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addStatusIdExcludeListItem(UUID item) {
        this.statusIdExcludeList = CollectionUtils.safeAdd(this.statusIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTagDataListOptionIdListItem(UUID item) {
        this.tagDataListOptionIdList = CollectionUtils.safeAdd(this.tagDataListOptionIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTagDataListOptionIdExcludeListItem(UUID item) {
        this.tagDataListOptionIdExcludeList = CollectionUtils.safeAdd(this.tagDataListOptionIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addMarkerDataListOptionIdListItem(UUID item) {
        this.markerDataListOptionIdList = CollectionUtils.safeAdd(this.markerDataListOptionIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addMarkerDataListOptionIdExcludeListItem(UUID item) {
        this.markerDataListOptionIdExcludeList = CollectionUtils.safeAdd(this.markerDataListOptionIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTwinClassExtendsHierarchyContainsIdListItem(UUID item) {
        this.twinClassExtendsHierarchyContainsIdList = CollectionUtils.safeAdd(this.twinClassExtendsHierarchyContainsIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addHeadTwinClassIdListItem(UUID item) {
        this.headTwinClassIdList = CollectionUtils.safeAdd(this.headTwinClassIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTouchListItem(Touch item) {
        this.touchList = CollectionUtils.safeAdd(this.touchList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTouchExcludeListItem(Touch item) {
        this.touchExcludeList = CollectionUtils.safeAdd(this.touchExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 putFieldsItem(UUID key, TwinFieldSearchDTOv1 item) {
        if (this.fields == null) this.fields = new HashMap<>();
        this.fields.put(key, item);
        return this;
    }

}
