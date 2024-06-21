package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinClassSearch {
    Set<UUID> twinClassIdList;
    Set<String> twinClassKeyLikeList;
    Set<UUID> headTwinClassIdList;
    Set<UUID> extendsTwinClassIdList;
    TwinClassEntity.OwnerType ownerType;
    Ternary abstractt;
    Ternary twinflowSchemaSpace;
    Ternary twinClassSchemaSpace;
    Ternary permissionSchemaSpace;
    Ternary aliasSpace;

    public TwinClassSearch addTwinClassId(Collection<UUID> twinClassIdSet) {
        twinClassIdList = CollectionUtils.safeAdd(twinClassIdList, twinClassIdSet);
        return this;
    }

    public TwinClassSearch addTwinNameLike(Collection<String> twinNameLikeSet) {
        twinClassKeyLikeList = CollectionUtils.safeAdd(twinClassKeyLikeList, twinNameLikeSet);
        return this;
    }

    public TwinClassSearch addHeadTwinClassId(Collection<UUID> twinClassIdSet) {
        headTwinClassIdList = CollectionUtils.safeAdd(headTwinClassIdList, twinClassIdSet);
        return this;
    }

    public TwinClassSearch addExtendsTwinClassId(Collection<UUID> twinClassIdSet) {
        extendsTwinClassIdList = CollectionUtils.safeAdd(extendsTwinClassIdList, twinClassIdSet);
        return this;
    }

}
