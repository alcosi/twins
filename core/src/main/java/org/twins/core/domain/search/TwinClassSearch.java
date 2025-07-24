package org.twins.core.domain.search;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
    private Set<String> externalIdLikeList;
    private Set<String> externalIdNotLikeList;

    private HierarchySearch headHierarchyChildsForTwinClassSearch;
    private HierarchySearch headHierarchyParentsForTwinClassSearch;
    private HierarchySearch extendsHierarchyChildsForTwinClassSearch;
    private HierarchySearch extendsHierarchyParentsForTwinClassSearch;
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
    private Ternary assigneeRequired;
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

    public TwinClassSearch addTwinClassId(final UUID id) {
            if (twinClassIdList == null) {
                twinClassIdList = new java.util.HashSet<>();
            }
            twinClassIdList.add(id);
        return this;
    }

    public TwinClassSearch addAllTwinClassIds(Set<UUID> ids) {
        if (twinClassIdList == null) {
            twinClassIdList = new java.util.HashSet<>();
        }
        twinClassIdList.addAll(ids);
        return this;
    }

    public static final ImmutableList<Pair<Function<TwinClassSearch, Set>, BiConsumer<TwinClassSearch, Set>>> FUNCTIONS = ImmutableList.of(
            Pair.of(TwinClassSearch::getTwinClassIdList, TwinClassSearch::setTwinClassIdList),
            Pair.of(TwinClassSearch::getTwinClassKeyLikeList, TwinClassSearch::setTwinClassKeyLikeList),
            Pair.of(TwinClassSearch::getNameI18nLikeList, TwinClassSearch::setNameI18nLikeList),
            Pair.of(TwinClassSearch::getNameI18nNotLikeList, TwinClassSearch::setNameI18nNotLikeList),
            Pair.of(TwinClassSearch::getDescriptionI18nLikeList, TwinClassSearch::setDescriptionI18nLikeList),
            Pair.of(TwinClassSearch::getDescriptionI18nNotLikeList, TwinClassSearch::setDescriptionI18nNotLikeList),
            Pair.of(TwinClassSearch::getExternalIdLikeList, TwinClassSearch::setExternalIdLikeList),
            Pair.of(TwinClassSearch::getExternalIdNotLikeList, TwinClassSearch::setExternalIdNotLikeList),
            Pair.of(TwinClassSearch::getViewPermissionIdList, TwinClassSearch::setViewPermissionIdList),
            Pair.of(TwinClassSearch::getViewPermissionIdExcludeList, TwinClassSearch::setViewPermissionIdExcludeList),
            Pair.of(TwinClassSearch::getEditPermissionIdList, TwinClassSearch::setEditPermissionIdList),
            Pair.of(TwinClassSearch::getEditPermissionIdExcludeList, TwinClassSearch::setEditPermissionIdExcludeList)
    );
}
