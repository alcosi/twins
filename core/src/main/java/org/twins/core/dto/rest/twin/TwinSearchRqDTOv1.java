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
        CollectionUtils.safeAdd(twinClassIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTwinClassIdExcludeListItem(UUID item) {
        CollectionUtils.safeAdd(twinClassIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTwinNameLikeListItem(String item) {
        CollectionUtils.safeAdd(twinNameLikeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTwinNameNotLikeListItem(String item) {
        CollectionUtils.safeAdd(twinNameNotLikeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addDescriptionLikeListItem(String item) {
        CollectionUtils.safeAdd(descriptionLikeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addDescriptionNotLikeListItem(String item) {
        CollectionUtils.safeAdd(descriptionNotLikeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addHeadTwinIdListItem(UUID item) {
        CollectionUtils.safeAdd(headTwinIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTwinIdListItem(UUID item) {
        CollectionUtils.safeAdd(twinIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTwinIdExcludeListItem(UUID item) {
        CollectionUtils.safeAdd(twinIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addStatusIdListItem(UUID item) {
        CollectionUtils.safeAdd(statusIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addAssignerUserIdListItem(UUID item) {
        CollectionUtils.safeAdd(assignerUserIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addAssignerUserIdExcludeListItem(UUID item) {
        CollectionUtils.safeAdd(assignerUserIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addCreatedByUserIdListItem(UUID item) {
        CollectionUtils.safeAdd(createdByUserIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addCreatedByUserIdExcludeListItem(UUID item) {
        CollectionUtils.safeAdd(createdByUserIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addExternalIdListItem(String item) {
        CollectionUtils.safeAdd(externalIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addExternalIdExcludeListItem(String item) {
        CollectionUtils.safeAdd(externalIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addLinksAnyOfListItem(TwinSearchByLinkDTOv1 item) {
        CollectionUtils.safeAdd(linksAnyOfList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addLinksNoAnyOfListItem(TwinSearchByLinkDTOv1 item) {
        CollectionUtils.safeAdd(linksNoAnyOfList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addLinksAllOfListItem(TwinSearchByLinkDTOv1 item) {
        CollectionUtils.safeAdd(linksAllOfList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addLinksNoAllOfListItem(TwinSearchByLinkDTOv1 item) {
        CollectionUtils.safeAdd(linksNoAllOfList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addHierarchyTreeContainsIdListItem(UUID item) {
        CollectionUtils.safeAdd(hierarchyTreeContainsIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addStatusIdExcludeListItem(UUID item) {
        CollectionUtils.safeAdd(statusIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTagDataListOptionIdListItem(UUID item) {
        CollectionUtils.safeAdd(tagDataListOptionIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTagDataListOptionIdExcludeListItem(UUID item) {
        CollectionUtils.safeAdd(tagDataListOptionIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addMarkerDataListOptionIdListItem(UUID item) {
        CollectionUtils.safeAdd(markerDataListOptionIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addMarkerDataListOptionIdExcludeListItem(UUID item) {
        CollectionUtils.safeAdd(markerDataListOptionIdExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTwinClassExtendsHierarchyContainsIdListItem(UUID item) {
        CollectionUtils.safeAdd(twinClassExtendsHierarchyContainsIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addHeadTwinClassIdListItem(UUID item) {
        CollectionUtils.safeAdd(headTwinClassIdList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTouchListItem(Touch item) {
        CollectionUtils.safeAdd(touchList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 addTouchExcludeListItem(Touch item) {
        CollectionUtils.safeAdd(touchExcludeList, item);
        return this;
    }

    @Override
    public TwinSearchRqDTOv1 putFieldsItem(UUID key, TwinFieldSearchDTOv1 item) {
        if (this.fields == null) this.fields = new HashMap<>();
        this.fields.put(key, item);
        return this;
    }


}
