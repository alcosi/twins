package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinClassSearch {
    private Set<UUID> twinClassIdList;
    private Set<UUID> twinClassIdExcludeList;
    private Set<String> twinClassKeyLikeList;
    private Set<String> nameI18nLikeList;
    private Set<String> nameI18nNotLikeList;
    private Set<String> descriptionI18nLikeList;
    private Set<String> descriptionI18nNotLikeList;
    private Set<UUID> headTwinClassIdList;
    private Set<UUID> headTwinClassIdExcludeList;
    private Set<UUID> extendsTwinClassIdList;
    private Set<UUID> extendsTwinClassIdExcludeList;
    private Set<TwinClassEntity.OwnerType> ownerTypeList;
    private Set<TwinClassEntity.OwnerType> ownerTypeExcludeList;
    private Set<UUID> markerDatalistIdList;
    private Set<UUID> markerDatalistIdExcludeList;
    private Set<UUID> tagDatalistIdList;
    private Set<UUID> tagDatalistIdExcludeList;
    private Ternary abstractt;
    private Ternary twinflowSchemaSpace;
    private Ternary twinClassSchemaSpace;
    private Ternary permissionSchemaSpace;
    private Ternary aliasSpace;
    private Set<UUID> viewPermissionIdList;
    private Set<UUID> viewPermissionIdExcludeList;
    private Set<UUID> createPermissionIdList;
    private Set<UUID> createPermissionIdExcludeList;
    private Set<UUID> editPermissionIdList;
    private Set<UUID> editPermissionIdExcludeList;
    private Set<UUID> deletePermissionIdList;
    private Set<UUID> deletePermissionIdExcludeList;

    public TwinClassSearch addOwnerTypeExclude() {
        ownerTypeExcludeList = CollectionUtils.safeAdd(ownerTypeExcludeList, TwinClassEntity.OwnerType.SYSTEM);
        return this;
    }
}
