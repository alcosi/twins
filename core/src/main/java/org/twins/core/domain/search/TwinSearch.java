package org.twins.core.domain.search;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.math.IntegerRange;
import org.twins.core.dao.search.TwinSearchEntity;
import org.twins.core.domain.DataTimeRange;
import org.twins.core.domain.apiuser.DBUMembershipCheck;
import org.twins.core.enums.twin.Touch;

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
    private DBUMembershipCheck dbuMembershipCheck; // this will take sense only if we search by TWIN_CLASS_USER or TWIN_CLASS_BUSINESS_ACCOUNT
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
    private Map<UUID, Set<UUID>> dstLinksAnyOfList;
    private Map<UUID, Set<UUID>> dstLinksNoAnyOfList;
    private Map<UUID, Set<UUID>> dstLinksAllOfList;
    private Map<UUID, Set<UUID>> dstLinksNoAllOfList;
    private Map<UUID, Set<UUID>> srcLinksAnyOfList;
    private Map<UUID, Set<UUID>> srcLinksNoAnyOfList;
    private Map<UUID, Set<UUID>> srcLinksAllOfList;
    private Map<UUID, Set<UUID>> srcLinksNoAllOfList;
    private Set<UUID> hierarchyTreeContainsIdList;
    private Set<UUID> statusIdExcludeList;
    private Set<UUID> tagDataListOptionIdList;
    private Set<UUID> tagDataListOptionIdExcludeList;
    private Set<UUID> markerDataListOptionIdList;
    private Set<UUID> markerDataListOptionIdExcludeList;
    private Set<Touch> touchList;
    private Set<Touch> touchExcludeList;
    private List<TwinFieldSearch> fields;
    private DataTimeRange createdAt;
    private TwinSearchEntity configuredSearch;
    private HierarchySearch hierarchyChildrenSearch;
    private Boolean distinct;
    private IntegerRange headHierarchyCounterDirectChildrenRange;
    // if true, status check will consider freeze status from twin class (freeze status has priority over native twin status)
    private boolean checkFreezeStatus = true;

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(twinIdList) &&
                CollectionUtils.isEmpty(twinNameLikeList) &&
                CollectionUtils.isEmpty(twinNameNotLikeList) &&
                CollectionUtils.isEmpty(twinDescriptionLikeList) &&
                CollectionUtils.isEmpty(twinDescriptionNotLikeList) &&
                CollectionUtils.isEmpty(externalIdList) &&
                CollectionUtils.isEmpty(externalIdExcludeList) &&
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
                CollectionUtils.isEmpty(dstLinksAnyOfList) &&
                CollectionUtils.isEmpty(dstLinksNoAnyOfList) &&
                CollectionUtils.isEmpty(dstLinksAllOfList) &&
                CollectionUtils.isEmpty(dstLinksNoAllOfList) &&
                CollectionUtils.isEmpty(srcLinksAnyOfList) &&
                CollectionUtils.isEmpty(srcLinksNoAnyOfList) &&
                CollectionUtils.isEmpty(srcLinksAllOfList) &&
                CollectionUtils.isEmpty(srcLinksNoAllOfList) &&
                CollectionUtils.isEmpty(hierarchyTreeContainsIdList) &&
                CollectionUtils.isEmpty(statusIdExcludeList) &&
                CollectionUtils.isEmpty(tagDataListOptionIdList) &&
                CollectionUtils.isEmpty(tagDataListOptionIdExcludeList) &&
                CollectionUtils.isEmpty(markerDataListOptionIdList) &&
                CollectionUtils.isEmpty(markerDataListOptionIdExcludeList) &&
                CollectionUtils.isEmpty(touchList) &&
                CollectionUtils.isEmpty(touchExcludeList) &&
                CollectionUtils.isEmpty(fields) &&
                (hierarchyChildrenSearch == null || hierarchyChildrenSearch.isEmpty()) &&
                createdAt == null &&
                distinct == null &&
                isHeadHierarchyCounterDirectChildrenRangeEmpty();
    }

    private boolean isHeadHierarchyCounterDirectChildrenRangeEmpty() {
        if (headHierarchyCounterDirectChildrenRange == null) return true;
        return headHierarchyCounterDirectChildrenRange.getFrom() == null && headHierarchyCounterDirectChildrenRange.getTo() == null;
    }

    public TwinSearch addTwinId(UUID twinId, boolean exclude) {
        if (exclude)
            twinIdExcludeList = CollectionUtils.safeAdd(twinIdExcludeList, twinId);
        else
            twinIdList = CollectionUtils.safeAdd(twinIdList, twinId);
        return this;
    }

    public TwinSearch addTwinId(Collection<UUID> twinIds, boolean exclude) {
        if (exclude)
            twinIdExcludeList = CollectionUtils.safeAdd(twinIdExcludeList, twinIds);
        else
            twinIdList = CollectionUtils.safeAdd(twinIdList, twinIds);
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

    public TwinSearch addHeadTwinId(UUID headerTwinId) {
        headTwinIdList = CollectionUtils.safeAdd(headTwinIdList, headerTwinId);
        return this;
    }

    public TwinSearch addHeadTwinId(Collection<UUID> headerTwinIds) {
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

    public TwinSearch addAssigneeUserId(Collection<UUID>  assigneeUserIds, boolean exclude) {
        if (exclude)
            assigneeUserIdList = CollectionUtils.safeAdd(assigneeUserIdList, assigneeUserIds);
        else
            assigneeUserIdExcludeList = CollectionUtils.safeAdd(assigneeUserIdExcludeList, assigneeUserIds);
        return this;
    }

    public TwinSearch addCreatedByUserId(UUID createdByUserId, boolean exclude) {
        if (exclude)
            createdByUserIdList = CollectionUtils.safeAdd(createdByUserIdList, createdByUserId);
        else
            createdByUserIdExcludeList = CollectionUtils.safeAdd(createdByUserIdExcludeList, createdByUserId);
        return this;
    }

    public TwinSearch addCreatedByUserId(Collection<UUID> createdByUserIds, boolean exclude) {
        if (exclude)
            createdByUserIdList = CollectionUtils.safeAdd(createdByUserIdList, createdByUserIds);
        else
            createdByUserIdExcludeList = CollectionUtils.safeAdd(createdByUserIdExcludeList, createdByUserIds);
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
                if (dstLinksNoAnyOfList == null) dstLinksNoAnyOfList = new HashMap<>();
                map = dstLinksNoAnyOfList;
            } else {
                if (dstLinksNoAllOfList == null) dstLinksNoAllOfList = new HashMap<>();
                map = dstLinksNoAllOfList;
            }
        } else {
            if (or) {
                if (dstLinksAnyOfList == null) dstLinksAnyOfList = new HashMap<>();
                map = dstLinksAnyOfList;
            } else {
                if (dstLinksAllOfList == null) dstLinksAllOfList = new HashMap<>();
                map = dstLinksAllOfList;
            }
        }
        map.computeIfAbsent(linkId, k -> new HashSet<>()).addAll(null != dstTwinIdList ? dstTwinIdList : Collections.emptySet());
        return this;
    }

    public TwinSearch addLinkSrcTwinsId(UUID linkId, Collection<UUID> srcTwinIdList, boolean exclude, boolean or) {
        Map<UUID, Set<UUID>> map = null;
        if (exclude) {
            if (or) {
                if (srcLinksNoAnyOfList == null) srcLinksNoAnyOfList = new HashMap<>();
                map = srcLinksNoAnyOfList;
            } else {
                if (srcLinksNoAllOfList == null) srcLinksNoAllOfList = new HashMap<>();
                map = srcLinksNoAllOfList;
            }
        } else {
            if (or) {
                if (srcLinksAnyOfList == null) srcLinksAnyOfList = new HashMap<>();
                map = srcLinksAnyOfList;
            } else {
                if (srcLinksAllOfList == null) srcLinksAllOfList = new HashMap<>();
                map = srcLinksAllOfList;
            }
        }
        map.computeIfAbsent(linkId, k -> new HashSet<>()).addAll(null != srcTwinIdList ? srcTwinIdList : Collections.emptySet());
        return this;
    }

    public TwinSearch addHierarchyTreeContainsId(UUID twinId) {
        hierarchyTreeContainsIdList = CollectionUtils.safeAdd(hierarchyTreeContainsIdList, twinId);
        return this;
    }

    public TwinSearch addTwinClassExtendsHierarchyContainsId(UUID twinClassId) {
        twinClassExtendsHierarchyContainsIdList = CollectionUtils.safeAdd(twinClassExtendsHierarchyContainsIdList, twinClassId);
        return this;
    }

    public TwinSearch addTwinClassExtendsHierarchyContainsId(Collection<UUID> twinClassIdSet) {
        twinClassExtendsHierarchyContainsIdList = CollectionUtils.safeAdd(twinClassExtendsHierarchyContainsIdList, twinClassIdSet);
        return this;
    }

    public TwinSearch addMarkerDataListOptionId(UUID markerDataListOptionId, boolean exclude) {
        if (exclude)
            markerDataListOptionIdExcludeList = CollectionUtils.safeAdd(markerDataListOptionIdExcludeList, markerDataListOptionId);
        else
            markerDataListOptionIdList = CollectionUtils.safeAdd(markerDataListOptionIdList, markerDataListOptionId);
        return this;
    }

    public TwinSearch addMarkerDataListOptionId(Collection<UUID> markerDataListOptionId, boolean exclude) {
        if (exclude)
            markerDataListOptionIdExcludeList = CollectionUtils.safeAdd(markerDataListOptionIdExcludeList, markerDataListOptionId);
        else
            markerDataListOptionIdList = CollectionUtils.safeAdd(markerDataListOptionIdList, markerDataListOptionId);
        return this;
    }

    public TwinSearch addTouchId(Touch touchId, boolean exclude) {//todo need use in SearchCriteriaBuilderSingleUUID ???
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

    public TwinSearch addTagDataListOptionId(Collection<UUID> tagDataListOptionIds, boolean exclude) {
        if (exclude)
            tagDataListOptionIdExcludeList = CollectionUtils.safeAdd(tagDataListOptionIdExcludeList, tagDataListOptionIds);
        else
            tagDataListOptionIdList = CollectionUtils.safeAdd(tagDataListOptionIdList, tagDataListOptionIds);
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
