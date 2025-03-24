package org.twins.core.domain.search;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.twin.TwinTouchEntity;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinSearch {
    private Set<UUID> twinIdList;
    private Set<String> twinNameLikeList;
    private Set<String> twinNameNotLikeList;
    private Set<String> twinDescriptionLikeList;
    private Set<String> twinDescriptionNotLikeList;
    private Set<String> externalIdList;
    private Set<String> externalIdExcludeList;
    private Set<UUID> twinIdExcludeList;
    private Set<UUID> twinClassIdList;
    private Set<UUID> twinClassIdExcludeList;
    private Set<UUID> headTwinClassIdList;
    private Set<UUID> twinClassExtendsHierarchyContainsIdList;
    private Set<UUID> headTwinIdList;
    private Set<UUID> statusIdList;
    private Set<UUID> assigneeUserIdList;
    private Set<UUID> assigneeUserIdExcludeList;
    private Set<UUID> createdByUserIdList;
    private Set<UUID> createdByUserIdExcludeList;
    private Set<UUID> ownerUserIdList;
    private Set<UUID> ownerBusinessAccountIdList;
    private Map<UUID, Set<UUID>> linksAnyOfList;
    private Map<UUID, Set<UUID>> linksNoAnyOfList;
    private Map<UUID, Set<UUID>> linksAllOfList;
    private Map<UUID, Set<UUID>> linksNoAllOfList;
    private Set<UUID> hierarchyTreeContainsIdList;
    private Set<UUID> statusIdExcludeList;
    private Set<UUID> tagDataListOptionIdList;
    private Set<UUID> tagDataListOptionIdExcludeList;
    private Set<UUID> markerDataListOptionIdList;
    private Set<UUID> markerDataListOptionIdExcludeList;
    private Set<TwinTouchEntity.Touch> touchList;
    private Set<TwinTouchEntity.Touch> touchExcludeList;
    private List<TwinFieldSearch> fields;

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(twinIdList) &&
                CollectionUtils.isEmpty(twinNameLikeList) &&
                CollectionUtils.isEmpty(twinNameNotLikeList) &&
                CollectionUtils.isEmpty(twinDescriptionLikeList) &&
                CollectionUtils.isEmpty(twinDescriptionNotLikeList) &&
                CollectionUtils.isEmpty(twinIdExcludeList) &&
                CollectionUtils.isEmpty(twinClassIdList) &&
                CollectionUtils.isEmpty(twinClassIdExcludeList) &&
                CollectionUtils.isEmpty(headTwinClassIdList) &&
                CollectionUtils.isEmpty(twinClassExtendsHierarchyContainsIdList) &&
                CollectionUtils.isEmpty(headTwinIdList) &&
                CollectionUtils.isEmpty(statusIdList) &&
                CollectionUtils.isEmpty(assigneeUserIdList) &&
                CollectionUtils.isEmpty(assigneeUserIdExcludeList) &&
                CollectionUtils.isEmpty(createdByUserIdList) &&
                CollectionUtils.isEmpty(createdByUserIdExcludeList) &&
                CollectionUtils.isEmpty(ownerUserIdList) &&
                CollectionUtils.isEmpty(ownerBusinessAccountIdList) &&
                CollectionUtils.isEmpty(linksAnyOfList) &&
                CollectionUtils.isEmpty(linksNoAnyOfList) &&
                CollectionUtils.isEmpty(linksAllOfList) &&
                CollectionUtils.isEmpty(linksNoAllOfList) &&
                CollectionUtils.isEmpty(hierarchyTreeContainsIdList) &&
                CollectionUtils.isEmpty(statusIdExcludeList) &&
                CollectionUtils.isEmpty(tagDataListOptionIdList) &&
                CollectionUtils.isEmpty(tagDataListOptionIdExcludeList) &&
                CollectionUtils.isEmpty(markerDataListOptionIdList) &&
                CollectionUtils.isEmpty(markerDataListOptionIdExcludeList) &&
                CollectionUtils.isEmpty(touchList) &&
                CollectionUtils.isEmpty(touchExcludeList) &&
                CollectionUtils.isEmpty(fields);
    }

    public TwinSearch addTwinId(UUID twinId, boolean exclude) {
        if (exclude)
            twinIdExcludeList = CollectionUtils.safeAdd(twinIdExcludeList, twinId);
        else
            twinIdList = CollectionUtils.safeAdd(twinIdList, twinId);
        return this;
    }

    public TwinSearch addTwinNameLike(String twinNameLike) {
        twinNameLikeList = CollectionUtils.safeAdd(twinNameLikeList, twinNameLike);
        return this;
    }

    public TwinSearch addTwinClassId(UUID twinClassId, boolean exclude) {
        if (exclude)
            twinClassIdExcludeList = CollectionUtils.safeAdd(twinClassIdExcludeList, twinClassId);
        else
            twinClassIdList = CollectionUtils.safeAdd(twinClassIdList, twinClassId);
        return this;
    }

    public TwinSearch addTwinClassId(Collection<UUID> twinClassIdSet, boolean exclude) {
        if (exclude)
            twinClassIdExcludeList = CollectionUtils.safeAdd(twinClassIdExcludeList, twinClassIdSet);
        else
            twinClassIdList = CollectionUtils.safeAdd(twinClassIdList, twinClassIdSet);
        return this;
    }

    public TwinSearch addHeaderTwinId(UUID headerTwinId) {
        headTwinIdList = CollectionUtils.safeAdd(headTwinIdList, headerTwinId);
        return this;
    }

    public TwinSearch addHeaderTwinId(List<UUID> headerTwinIds) {
        headTwinIdList = CollectionUtils.safeAdd(headTwinIdList, headerTwinIds);
        return this;
    }



    public TwinSearch addStatusId(UUID statusId, boolean exclude) {
        if (exclude)
            statusIdExcludeList = CollectionUtils.safeAdd(statusIdExcludeList, statusId);
        else
            statusIdList = CollectionUtils.safeAdd(statusIdList, statusId);
        return this;
    }

    public TwinSearch addStatusId(Collection<UUID> statusIdSet, boolean exclude) {
        if (exclude)
            statusIdExcludeList = CollectionUtils.safeAdd(statusIdExcludeList, statusIdSet);
        else
            statusIdList = CollectionUtils.safeAdd(statusIdList, statusIdSet);
        return this;
    }

    public TwinSearch addAssigneeUserId(UUID assigneeUserId, boolean exclude) {
        if (exclude)
            assigneeUserIdList = CollectionUtils.safeAdd(assigneeUserIdList, assigneeUserId);
        else
            assigneeUserIdExcludeList = CollectionUtils.safeAdd(assigneeUserIdExcludeList, assigneeUserId);
        return this;
    }

    public TwinSearch addCreatedByUserId(UUID createdByUserId, boolean exclude) {
        if (exclude)
            createdByUserIdList = CollectionUtils.safeAdd(createdByUserIdList, createdByUserId);
        else
            createdByUserIdExcludeList = CollectionUtils.safeAdd(createdByUserIdExcludeList, createdByUserId);
        return this;
    }

    public TwinSearch addOwnerUserId(UUID ownerUserId) {
        ownerUserIdList = CollectionUtils.safeAdd(ownerUserIdList, ownerUserId);
        return this;
    }

    public TwinSearch addOwnerBusinessAccountId(UUID ownerBusinessAccountId) {
        ownerBusinessAccountIdList = CollectionUtils.safeAdd(ownerBusinessAccountIdList, ownerBusinessAccountId);
        return this;
    }

    public TwinSearch addLinkDstTwinsId(UUID linkId, Collection<UUID> dstTwinIdList, boolean exclude, boolean or) {
        Map<UUID, Set<UUID>> map = null;
        if (exclude) {
            if (or) {
                if (linksNoAnyOfList == null) linksNoAnyOfList = new HashMap<>();
                map = linksNoAnyOfList;
            } else {
                if (linksNoAllOfList == null) linksNoAllOfList = new HashMap<>();
                map = linksNoAllOfList;
            }
        } else {
            if (or) {
                if (linksAnyOfList == null) linksAnyOfList = new HashMap<>();
                map = linksAnyOfList;
            } else {
                if (linksAllOfList == null) linksAllOfList = new HashMap<>();
                map = linksAllOfList;
            }
        }
        map.computeIfAbsent(linkId, k -> new HashSet<>()).addAll(null != dstTwinIdList ? dstTwinIdList : Collections.emptySet());
        return this;
    }

    public TwinSearch addHierarchyTreeContainsId(UUID twinId) {
        hierarchyTreeContainsIdList = CollectionUtils.safeAdd(hierarchyTreeContainsIdList, twinId);
        return this;
    }

    public TwinSearch addMarkerDataListOptionId(UUID markerDataListOptionId, boolean exclude) {
        if (exclude)
            markerDataListOptionIdExcludeList = CollectionUtils.safeAdd(markerDataListOptionIdExcludeList, markerDataListOptionId);
        else
            markerDataListOptionIdList = CollectionUtils.safeAdd(markerDataListOptionIdList, markerDataListOptionId);
        return this;
    }

    public TwinSearch addTouchId(TwinTouchEntity.Touch touchId, boolean exclude) {//todo need use in SearchCriteriaBuilderSingleUUID ???
        if (exclude)
            touchExcludeList = CollectionUtils.safeAdd(touchExcludeList, touchId);
        else
            touchList = CollectionUtils.safeAdd(touchList, touchId);
        return this;
    }

    public TwinSearch addTagDataListOptionId(UUID tagDataListOptionId, boolean exclude) {
        if (exclude)
            tagDataListOptionIdExcludeList = CollectionUtils.safeAdd(tagDataListOptionIdExcludeList, tagDataListOptionId);
        else
            tagDataListOptionIdList = CollectionUtils.safeAdd(tagDataListOptionIdList, tagDataListOptionId);
        return this;
    }

    public static final ImmutableList<Pair<Function<TwinSearch, Set<UUID>>, BiConsumer<TwinSearch, Set<UUID>>>> FUNCTIONS = ImmutableList.of(
            Pair.of(TwinSearch::getHeadTwinIdList, TwinSearch::setHeadTwinIdList),
            Pair.of(TwinSearch::getCreatedByUserIdList, TwinSearch::setCreatedByUserIdList),
            Pair.of(TwinSearch::getCreatedByUserIdExcludeList, TwinSearch::setCreatedByUserIdExcludeList),
            Pair.of(TwinSearch::getAssigneeUserIdList, TwinSearch::setAssigneeUserIdList),
            Pair.of(TwinSearch::getAssigneeUserIdExcludeList, TwinSearch::setAssigneeUserIdExcludeList),
            Pair.of(TwinSearch::getMarkerDataListOptionIdList, TwinSearch::setMarkerDataListOptionIdList),
            Pair.of(TwinSearch::getMarkerDataListOptionIdExcludeList, TwinSearch::setMarkerDataListOptionIdExcludeList),
            Pair.of(TwinSearch::getTagDataListOptionIdList, TwinSearch::setTagDataListOptionIdList),
            Pair.of(TwinSearch::getTagDataListOptionIdExcludeList, TwinSearch::setTagDataListOptionIdExcludeList),
            Pair.of(TwinSearch::getTwinIdList, TwinSearch::setTwinIdList),
            Pair.of(TwinSearch::getTwinIdExcludeList, TwinSearch::setTwinIdExcludeList),
            Pair.of(TwinSearch::getOwnerBusinessAccountIdList, TwinSearch::setOwnerBusinessAccountIdList),
            Pair.of(TwinSearch::getOwnerUserIdList, TwinSearch::setOwnerUserIdList),
            Pair.of(TwinSearch::getStatusIdList, TwinSearch::setStatusIdList),
            Pair.of(TwinSearch::getStatusIdExcludeList, TwinSearch::setStatusIdExcludeList),
            Pair.of(TwinSearch::getTwinClassIdList, TwinSearch::setTwinClassIdList),
            Pair.of(TwinSearch::getTwinClassIdExcludeList, TwinSearch::setTwinClassIdExcludeList),
            Pair.of(TwinSearch::getHierarchyTreeContainsIdList, TwinSearch::setHierarchyTreeContainsIdList),
            Pair.of(TwinSearch::getTwinClassExtendsHierarchyContainsIdList, TwinSearch::setTwinClassExtendsHierarchyContainsIdList),
            Pair.of(TwinSearch::getHeadTwinClassIdList, TwinSearch::setHeadTwinClassIdList)
    );

}
