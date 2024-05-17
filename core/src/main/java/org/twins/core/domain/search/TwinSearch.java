package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

import java.util.*;

@Data
@Accessors(chain = true)
public class TwinSearch {
    Set<UUID> twinIdList;
    Set<String> twinNameLikeList;
    Set<UUID> twinIdExcludeList;
    Set<UUID> twinClassIdList;
    Set<UUID> twinClassIdExcludeList;
    Set<UUID> headerTwinIdList;
    Set<UUID> statusIdList;
    Set<UUID> assignerUserIdList;
    Set<UUID> assignerUserIdExcludeList;
    Set<UUID> createdByUserIdList;
    Set<UUID> createdByUserIdExcludeList;
    Set<UUID> ownerUserIdList;
    Set<UUID> ownerBusinessAccountIdList;
    Map<UUID, Set<UUID>> twinLinksMap;
    Map<UUID, Set<UUID>> twinNoLinksMap;
    Set<UUID> hierarchyTreeContainsIdList;
    Set<UUID> statusIdExcludeList;
    Set<UUID> tagDataListOptionIdList;
    Set<UUID> tagDataListOptionIdExcludeList;
    Set<UUID> markerDataListOptionIdList;
    Set<UUID> markerDataListOptionIdExcludeList;

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
        headerTwinIdList = CollectionUtils.safeAdd(headerTwinIdList, headerTwinId);
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

    public TwinSearch addAssignerUserId(UUID assignerUserId, boolean exclude) {
        if (exclude)
            assignerUserIdList = CollectionUtils.safeAdd(assignerUserIdList, assignerUserId);
        else
            assignerUserIdExcludeList = CollectionUtils.safeAdd(assignerUserIdExcludeList, assignerUserId);
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

    public TwinSearch addLinkDstTwinsId(UUID linkId, List<UUID> dstTwinIdList) {
        if (twinLinksMap == null) twinLinksMap = new HashMap<>();
        twinLinksMap.computeIfAbsent(linkId, k -> new HashSet<>()).addAll(null != dstTwinIdList ? dstTwinIdList : Collections.emptySet());
        return this;
    }

    public TwinSearch addNoLinkDstTwinsId(UUID linkId, List<UUID> dstTwinIdList) {
        if (twinNoLinksMap == null) twinNoLinksMap = new HashMap<>();
        twinNoLinksMap.computeIfAbsent(linkId, k -> new HashSet<>()).addAll(null != dstTwinIdList ? dstTwinIdList : Collections.emptySet());
        return this;
    }

    public TwinSearch addHierarchyTreeContainsId(UUID twinId) {
        hierarchyTreeContainsIdList = CollectionUtils.safeAdd(hierarchyTreeContainsIdList, twinId);
        return this;
    }
}
