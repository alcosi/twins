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
    Set<UUID> twinClassIdExcludeList;
    Set<String> twinClassKeyLikeList;
    Set<String> nameI18nLikeList;
    Set<String> nameI18nNotLikeList;
    Set<String> descriptionI18nLikeList;
    Set<String> descriptionI18nNotLikeList;
    Set<UUID> headTwinClassIdList;
    Set<UUID> headTwinClassIdExcludeList;
    Set<UUID> extendsTwinClassIdList;
    Set<UUID> extendsTwinClassIdExcludeList;
    Set<TwinClassEntity.OwnerType> ownerTypeList;
    Set<TwinClassEntity.OwnerType> ownerTypeExcludeList;
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

    public TwinClassSearch addOwnerTypeExclude() {
        ownerTypeExcludeList = CollectionUtils.safeAdd(ownerTypeExcludeList, TwinClassEntity.OwnerType.SYSTEM);
        return this;
    }
}
