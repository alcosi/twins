package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

import java.util.*;

@Data
@Accessors(chain = true)
public class BasicSearch {
    Set<UUID> twinIdList;

    Set<String> twinNameLikeList;
    Set<UUID> twinIdExcludeList;
    Set<UUID> twinClassIdList;
    Set<UUID> twinClassIdExcludeList;
    Set<UUID> headerTwinIdList;
    Set<UUID> statusIdList;
    Set<UUID> assignerUserIdList;
    Set<UUID> createdByUserIdList;
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

    public BasicSearch addTwinId(UUID twinId) {
        twinIdList = CollectionUtils.safeAdd(twinIdList, twinId);
        return this;
    }

    public BasicSearch addTwinNameLike(String twinNameLike) {
        twinNameLikeList = CollectionUtils.safeAdd(twinNameLikeList, twinNameLike);
        return this;
    }

    public BasicSearch addTwinExcludeId(UUID twinId) {
        twinIdExcludeList = CollectionUtils.safeAdd(twinIdExcludeList, twinId);
        return this;
    }

    public BasicSearch addTwinClassId(UUID twinClassId) {
        twinClassIdList = CollectionUtils.safeAdd(twinClassIdList, twinClassId);
        return this;
    }

    public BasicSearch addTwinClassId(Collection<UUID> twinClassIdSet) {
        twinClassIdList = CollectionUtils.safeAdd(twinClassIdList, twinClassIdSet);
        return this;
    }

    public BasicSearch addHeaderTwinId(UUID headerTwinId) {
        headerTwinIdList = CollectionUtils.safeAdd(headerTwinIdList, headerTwinId);
        return this;
    }

    public BasicSearch addStatusId(UUID statusId) {
        statusIdList = CollectionUtils.safeAdd(statusIdList, statusId);
        return this;
    }

    public BasicSearch addStatusId(Collection<UUID> statusIdList) {
        if (this.statusIdList == null)
            this.statusIdList = new HashSet<>();
        this.statusIdList.addAll(statusIdList);
        return this;
    }

    public BasicSearch addAssignerUserId(UUID assignerUserId) {
        assignerUserIdList = CollectionUtils.safeAdd(assignerUserIdList, assignerUserId);
        return this;
    }

    public BasicSearch addCreatedByUserId(UUID createdByUserId) {
        createdByUserIdList = CollectionUtils.safeAdd(createdByUserIdList, createdByUserId);
        return this;
    }

    public BasicSearch addOwnerUserId(UUID ownerUserId) {
        ownerUserIdList = CollectionUtils.safeAdd(ownerUserIdList, ownerUserId);
        return this;
    }

    public BasicSearch addOwnerBusinessAccountId(UUID ownerBusinessAccountId) {
        ownerBusinessAccountIdList = CollectionUtils.safeAdd(ownerBusinessAccountIdList, ownerBusinessAccountId);
        return this;
    }

    public BasicSearch addLinkDstTwinsId(UUID linkId, List<UUID> dstTwinIdList) {
        if (twinLinksMap == null) twinLinksMap = new HashMap<>();
        twinLinksMap.computeIfAbsent(linkId, k -> new HashSet<>()).addAll(null != dstTwinIdList ? dstTwinIdList : Collections.emptySet());
        return this;
    }

    public BasicSearch addNoLinkDstTwinsId(UUID linkId, List<UUID> dstTwinIdList) {
        if (twinNoLinksMap == null) twinNoLinksMap = new HashMap<>();
        twinNoLinksMap.computeIfAbsent(linkId, k -> new HashSet<>()).addAll(null != dstTwinIdList ? dstTwinIdList : Collections.emptySet());
        return this;
    }

    public BasicSearch addHierarchyTreeContainsId(UUID twinId) {
        hierarchyTreeContainsIdList = CollectionUtils.safeAdd(hierarchyTreeContainsIdList, twinId);
        return this;
    }

    public BasicSearch addStatusIdExclude(UUID statusId) {
        statusIdExcludeList = CollectionUtils.safeAdd(statusIdExcludeList, statusId);
        return this;
    }

}
