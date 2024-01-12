package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;

@Data
@Accessors(chain = true)
public class BasicSearch {
    Set<UUID> twinIdList;
    Set<UUID> twinIdExcludeList;
    Set<UUID> twinClassIdList;
    Set<UUID> extendsTwinClassIdList;
    Set<UUID> headerTwinIdList;
    Set<UUID> statusIdList;
    Set<UUID> assignerUserIdList;
    Set<UUID> createdByUserIdList;
    Set<UUID> ownerUserIdList;
    Set<UUID> ownerBusinessAccountIdList;
    Map<UUID, Set<UUID>> twinLinksMap;

    public BasicSearch addTwinId(UUID twinId) {
        twinIdList = safeAdd(twinIdList, twinId);
        return this;
    }

    public BasicSearch addTwinExcludeId(UUID twinId) {
        twinIdExcludeList = safeAdd(twinIdExcludeList, twinId);
        return this;
    }

    public BasicSearch addTwinClassId(UUID twinClassId) {
        twinClassIdList = safeAdd(twinClassIdList, twinClassId);
        return this;
    }

    public BasicSearch addTwinClassId(Collection<UUID> twinClassIdSet) {
        twinClassIdList = safeAdd(twinClassIdList, twinClassIdSet);
        return this;
    }

    public BasicSearch addHeaderTwinId(UUID headerTwinId) {
        headerTwinIdList = safeAdd(headerTwinIdList, headerTwinId);
        return this;
    }

    public BasicSearch addStatusId(UUID statusId) {
        statusIdList = safeAdd(statusIdList, statusId);
        return this;
    }

    public BasicSearch addStatusId(Collection<UUID> statusIdList) {
        if (this.statusIdList == null)
            this.statusIdList = new HashSet<>();
        this.statusIdList.addAll(statusIdList);
        return this;
    }

    public BasicSearch addAssignerUserId(UUID assignerUserId) {
        assignerUserIdList = safeAdd(assignerUserIdList, assignerUserId);
        return this;
    }

    public BasicSearch addCreatedByUserId(UUID createdByUserId) {
        createdByUserIdList = safeAdd(createdByUserIdList, createdByUserId);
        return this;
    }

    public BasicSearch addOwnerUserId(UUID ownerUserId) {
        ownerUserIdList = safeAdd(ownerUserIdList, ownerUserId);
        return this;
    }

    public BasicSearch addOwnerBusinessAccountId(UUID ownerBusinessAccountId) {
        ownerBusinessAccountIdList = safeAdd(ownerBusinessAccountIdList, ownerBusinessAccountId);
        return this;
    }

    public BasicSearch addLinkDstTwinsId(UUID linkId, List<UUID> dstTwinIdList) {
        if (twinLinksMap == null) twinLinksMap = new HashMap<>();
        twinLinksMap.computeIfAbsent(linkId, k -> new HashSet<>()).addAll(dstTwinIdList);
        return this;
    }


    private Set<UUID> safeAdd(Set<UUID> set, UUID element) {
        if (set == null) set = new HashSet<>();
        set.add(element);
        return set;
    }

    private Set<UUID> safeAdd(Set<UUID> set, Collection<UUID> elements) {
        if (set == null) set = new HashSet<>();
        set.addAll(elements);
        return set;
    }
}
