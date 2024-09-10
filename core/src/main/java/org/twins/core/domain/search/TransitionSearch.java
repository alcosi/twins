package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TransitionSearch {
    List<UUID> twinClassIdList;
    List<UUID> twinClassIdExcludeList;
    List<UUID> twinflowIdList;
    List<UUID> twinflowIdExcludeList;
    List<UUID> srcStatusIdList;
    List<UUID> srcStatusIdExcludeList;
    List<UUID> dstStatusIdList;
    List<UUID> dstStatusIdExcludeList;
    List<String> aliasLikeList;
    List<UUID> permissionIdList;
    List<UUID> permissionIdExcludeList;
    List<UUID> inbuiltTwinFactoryIdList;
    List<UUID> inbuiltTwinFactoryIdExcludeList;
    List<UUID> draftingTwinFactoryIdList;
    List<UUID> draftingTwinFactoryIdExcludeList;

    public TransitionSearch addTwinClassId(UUID twinClassId, boolean exclude) {
        if (exclude)
            twinClassIdExcludeList = CollectionUtils.safeAdd(twinClassIdExcludeList, twinClassId);
        else
            twinClassIdList = CollectionUtils.safeAdd(twinClassIdList, twinClassId);
        return this;
    }

    public TransitionSearch addTwinClassId(Collection<UUID> twinClassIdSet, boolean exclude) {
        if (exclude)
            twinClassIdExcludeList = CollectionUtils.safeAdd(twinClassIdExcludeList, twinClassIdSet);
        else
            twinClassIdList = CollectionUtils.safeAdd(twinClassIdList, twinClassIdSet);
        return this;
    }

    public TransitionSearch addTwinflowId(UUID twinflowId, boolean exclude) {
        if (exclude)
            twinflowIdExcludeList = CollectionUtils.safeAdd(twinflowIdExcludeList, twinflowId);
        else
            twinflowIdList = CollectionUtils.safeAdd(twinflowIdList, twinflowId);
        return this;
    }

    public TransitionSearch addTwinflowId(Collection<UUID> twinflowIdSet, boolean exclude) {
        if (exclude)
            twinflowIdExcludeList = CollectionUtils.safeAdd(twinflowIdExcludeList, twinflowIdSet);
        else
            twinflowIdList = CollectionUtils.safeAdd(twinflowIdList, twinflowIdSet);
        return this;
    }

    public TransitionSearch addSrcStatusId(UUID srcStatusId, boolean exclude) {
        if (exclude)
            srcStatusIdExcludeList = CollectionUtils.safeAdd(srcStatusIdExcludeList, srcStatusId);
        else
            srcStatusIdList = CollectionUtils.safeAdd(srcStatusIdList, srcStatusId);
        return this;
    }

    public TransitionSearch addSrcStatusId(Collection<UUID> srcStatusIdSet, boolean exclude) {
        if (exclude)
            srcStatusIdExcludeList = CollectionUtils.safeAdd(srcStatusIdExcludeList, srcStatusIdSet);
        else
            srcStatusIdList = CollectionUtils.safeAdd(srcStatusIdList, srcStatusIdSet);
        return this;
    }

    public TransitionSearch addDstStatusId(UUID dstStatusId, boolean exclude) {
        if (exclude)
            dstStatusIdExcludeList = CollectionUtils.safeAdd(dstStatusIdExcludeList, dstStatusId);
        else
            dstStatusIdList = CollectionUtils.safeAdd(dstStatusIdList, dstStatusId);
        return this;
    }

    public TransitionSearch addDstStatusId(Collection<UUID> dstStatusIdSet, boolean exclude) {
        if (exclude)
            dstStatusIdExcludeList = CollectionUtils.safeAdd(dstStatusIdExcludeList, dstStatusIdSet);
        else
            dstStatusIdList = CollectionUtils.safeAdd(dstStatusIdList, dstStatusIdSet);
        return this;
    }

    public TransitionSearch addTransitionNameLike(String transitionNameLike) {
        aliasLikeList = CollectionUtils.safeAdd(aliasLikeList, transitionNameLike);
        return this;
    }

    public TransitionSearch addPermissionId(UUID permissionId, boolean exclude) {
        if (exclude)
            permissionIdExcludeList = CollectionUtils.safeAdd(permissionIdExcludeList, permissionId);
        else
            permissionIdList = CollectionUtils.safeAdd(permissionIdList, permissionId);
        return this;
    }

    public TransitionSearch addPermissionId(Collection<UUID> permissionIdSet, boolean exclude) {
        if (exclude)
            permissionIdExcludeList = CollectionUtils.safeAdd(permissionIdExcludeList, permissionIdSet);
        else
            permissionIdList = CollectionUtils.safeAdd(permissionIdList, permissionIdSet);
        return this;
    }

    public TransitionSearch addInbuiltTwinFactoryId(UUID inbuiltTwinFactoryId, boolean exclude) {
        if (exclude)
            inbuiltTwinFactoryIdExcludeList = CollectionUtils.safeAdd(inbuiltTwinFactoryIdExcludeList, inbuiltTwinFactoryId);
        else
            inbuiltTwinFactoryIdList = CollectionUtils.safeAdd(inbuiltTwinFactoryIdList, inbuiltTwinFactoryId);
        return this;
    }

    public TransitionSearch addInbuiltTwinFactoryId(Collection<UUID> inbuiltTwinFactoryIdSet, boolean exclude) {
        if (exclude)
            inbuiltTwinFactoryIdExcludeList = CollectionUtils.safeAdd(inbuiltTwinFactoryIdExcludeList, inbuiltTwinFactoryIdSet);
        else
            inbuiltTwinFactoryIdList = CollectionUtils.safeAdd(inbuiltTwinFactoryIdList, inbuiltTwinFactoryIdSet);
        return this;
    }

    public TransitionSearch addDraftingTwinFactoryId(UUID draftingTwinFactoryId, boolean exclude) {
        if (exclude)
            draftingTwinFactoryIdExcludeList = CollectionUtils.safeAdd(draftingTwinFactoryIdExcludeList, draftingTwinFactoryId);
        else
            draftingTwinFactoryIdList = CollectionUtils.safeAdd(draftingTwinFactoryIdList, draftingTwinFactoryId);
        return this;
    }

    public TransitionSearch addDraftingTwinFactoryId(Collection<UUID> draftingTwinFactoryIdSet, boolean exclude) {
        if (exclude)
            draftingTwinFactoryIdExcludeList = CollectionUtils.safeAdd(draftingTwinFactoryIdExcludeList, draftingTwinFactoryIdSet);
        else
            draftingTwinFactoryIdList = CollectionUtils.safeAdd(draftingTwinFactoryIdList, draftingTwinFactoryIdSet);
        return this;
    }

}
