package org.twins.core.domain.search;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.Ternary;
import org.twins.core.enums.twinclass.OwnerType;

import java.util.Collection;
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
    private Set<OwnerType> ownerTypeList;
    private Set<OwnerType> ownerTypeExcludeList;
    private Set<UUID> markerDatalistIdList;
    private Set<UUID> markerDatalistIdExcludeList;
    private Set<UUID> tagDatalistIdList;
    private Set<UUID> tagDatalistIdExcludeList;
    private Set<UUID> freezeIdList;
    private Set<UUID> freezeIdExcludeList;
    private Ternary abstractt;
    private Ternary twinflowSchemaSpace;
    private Ternary twinClassSchemaSpace;
    private Ternary permissionSchemaSpace;
    private Ternary aliasSpace;
    private Ternary assigneeRequired;
    private Ternary segment;
    private Ternary hasSegments;
    private Set<UUID> viewPermissionIdList;
    private Set<UUID> viewPermissionIdExcludeList;
    private Set<UUID> createPermissionIdList;
    private Set<UUID> createPermissionIdExcludeList;
    private Set<UUID> editPermissionIdList;
    private Set<UUID> editPermissionIdExcludeList;
    private Set<UUID> deletePermissionIdList;
    private Set<UUID> deletePermissionIdExcludeList;

    public TwinClassSearch addOwnerTypeExclude() {
        ownerTypeExcludeList = CollectionUtils.safeAdd(ownerTypeExcludeList, OwnerType.SYSTEM);
        return this;
    }

    public TwinClassSearch addTwinClassId(UUID twinClassId, boolean exclude) {
        if (exclude)
            twinClassIdExcludeList = CollectionUtils.safeAdd(twinClassIdExcludeList, twinClassId);
        else
            twinClassIdList = CollectionUtils.safeAdd(twinClassIdList, twinClassId);
        return this;
    }

    public TwinClassSearch addTwinClassId(Collection<UUID> twinClassIdSet, boolean exclude) {
        if (exclude)
            twinClassIdExcludeList = CollectionUtils.safeAdd(twinClassIdExcludeList, twinClassIdSet);
        else
            twinClassIdList = CollectionUtils.safeAdd(twinClassIdList, twinClassIdSet);
        return this;
    }

    public static final ImmutableList<Pair<Function<TwinClassSearch, Set>, BiConsumer<TwinClassSearch, Set>>> SET_FIELD = ImmutableList.of(
            Pair.of(TwinClassSearch::getTwinClassIdList, TwinClassSearch::setTwinClassIdList),
            Pair.of(TwinClassSearch::getTwinClassIdExcludeList, TwinClassSearch::setTwinClassIdExcludeList),
            Pair.of(TwinClassSearch::getTwinClassKeyLikeList, TwinClassSearch::setTwinClassKeyLikeList),
            Pair.of(TwinClassSearch::getNameI18nLikeList, TwinClassSearch::setNameI18nLikeList),
            Pair.of(TwinClassSearch::getNameI18nNotLikeList, TwinClassSearch::setNameI18nNotLikeList),
            Pair.of(TwinClassSearch::getDescriptionI18nLikeList, TwinClassSearch::setDescriptionI18nLikeList),
            Pair.of(TwinClassSearch::getDescriptionI18nNotLikeList, TwinClassSearch::setDescriptionI18nNotLikeList),
            Pair.of(TwinClassSearch::getExternalIdLikeList, TwinClassSearch::setExternalIdLikeList),
            Pair.of(TwinClassSearch::getExternalIdNotLikeList, TwinClassSearch::setExternalIdNotLikeList),
            Pair.of(TwinClassSearch::getOwnerTypeList, TwinClassSearch::setOwnerTypeList),
            Pair.of(TwinClassSearch::getOwnerTypeExcludeList, TwinClassSearch::setOwnerTypeExcludeList),
            Pair.of(TwinClassSearch::getMarkerDatalistIdList, TwinClassSearch::setMarkerDatalistIdList),
            Pair.of(TwinClassSearch::getMarkerDatalistIdExcludeList, TwinClassSearch::setMarkerDatalistIdExcludeList),
            Pair.of(TwinClassSearch::getTagDatalistIdExcludeList, TwinClassSearch::setTagDatalistIdExcludeList),
            Pair.of(TwinClassSearch::getTagDatalistIdExcludeList, TwinClassSearch::setTagDatalistIdExcludeList),
            Pair.of(TwinClassSearch::getViewPermissionIdList, TwinClassSearch::setViewPermissionIdList),
            Pair.of(TwinClassSearch::getViewPermissionIdExcludeList, TwinClassSearch::setViewPermissionIdExcludeList),
            Pair.of(TwinClassSearch::getCreatePermissionIdList, TwinClassSearch::setCreatePermissionIdList),
            Pair.of(TwinClassSearch::getCreatePermissionIdExcludeList, TwinClassSearch::setCreatePermissionIdExcludeList),
            Pair.of(TwinClassSearch::getEditPermissionIdList, TwinClassSearch::setEditPermissionIdList),
            Pair.of(TwinClassSearch::getEditPermissionIdExcludeList, TwinClassSearch::setEditPermissionIdExcludeList),
            Pair.of(TwinClassSearch::getDeletePermissionIdList, TwinClassSearch::setDeletePermissionIdList),
            Pair.of(TwinClassSearch::getDeletePermissionIdExcludeList, TwinClassSearch::setDeletePermissionIdExcludeList)
    );

    public static final ImmutableList<Pair<Function<TwinClassSearch, Ternary>, BiConsumer<TwinClassSearch, Ternary>>> TERNARY_FIELD = ImmutableList.of(
            Pair.of(TwinClassSearch::getAssigneeRequired, TwinClassSearch::setAssigneeRequired),
            Pair.of(TwinClassSearch::getTwinflowSchemaSpace, TwinClassSearch::setTwinflowSchemaSpace),
            Pair.of(TwinClassSearch::getTwinClassSchemaSpace, TwinClassSearch::setTwinClassSchemaSpace),
            Pair.of(TwinClassSearch::getPermissionSchemaSpace, TwinClassSearch::setPermissionSchemaSpace),
            Pair.of(TwinClassSearch::getAliasSpace, TwinClassSearch::setAliasSpace),
            Pair.of(TwinClassSearch::getAbstractt, TwinClassSearch::setAbstractt),
            Pair.of(TwinClassSearch::getSegment, TwinClassSearch::setSegment),
            Pair.of(TwinClassSearch::getHasSegments, TwinClassSearch::setHasSegments)
    );
}
