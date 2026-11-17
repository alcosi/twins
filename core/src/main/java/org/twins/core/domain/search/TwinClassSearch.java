package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.math.IntegerRange;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.SetUtils;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassSearchEntity;
import org.twins.core.enums.twinclass.OwnerType;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TwinClassSearch extends EntitySearch<TwinClassEntity> implements ConfiguredSearchHolder<TwinClassSearchEntity> {
    @SearchFilter
    private Set<UUID> twinClassIdList;

    @SearchFilter
    private Set<UUID> twinClassIdExcludeList;

    @SearchFilter
    private Set<String> twinClassKeyLikeList;

    @SearchFilter
    private Set<String> nameI18nLikeList;

    @SearchFilter
    private Set<String> nameI18nNotLikeList;

    @SearchFilter
    private Set<String> descriptionI18nLikeList;

    @SearchFilter
    private Set<String> descriptionI18nNotLikeList;

    @SearchFilter
    private Set<String> externalIdLikeList;

    @SearchFilter
    private Set<String> externalIdNotLikeList;

    @SearchFilter
    private Set<OwnerType> ownerTypeList;

    @SearchFilter
    private Set<OwnerType> ownerTypeExcludeList;

    @SearchFilter
    private Set<UUID> markerDatalistIdList;

    @SearchFilter
    private Set<UUID> markerDatalistIdExcludeList;

    @SearchFilter
    private Set<UUID> flavorDatalistIdList;

    @SearchFilter
    private Set<UUID> flavorDatalistIdExcludeList;

    @SearchFilter
    private Set<UUID> tagDatalistIdList;

    @SearchFilter
    private Set<UUID> tagDatalistIdExcludeList;

    @SearchFilter
    private Set<UUID> freezeIdList;

    @SearchFilter
    private Set<UUID> freezeIdExcludeList;

    @SearchFilter
    private Ternary abstractt;

    @SearchFilter
    private Ternary twinflowSchemaSpace;

    @SearchFilter
    private Ternary twinClassSchemaSpace;

    @SearchFilter
    private Ternary permissionSchemaSpace;

    @SearchFilter
    private Ternary aliasSpace;

    @SearchFilter
    private Ternary assigneeRequired;

    @SearchFilter
    private Ternary segment;

    @SearchFilter
    private Ternary hasSegments;

    @SearchFilter
    private Ternary uniqueName;

    @SearchFilter
    private Set<UUID> viewPermissionIdList;

    @SearchFilter
    private Set<UUID> viewPermissionIdExcludeList;

    @SearchFilter
    private Set<UUID> createPermissionIdList;

    @SearchFilter
    private Set<UUID> createPermissionIdExcludeList;

    @SearchFilter
    private IntegerRange twinCounterRange;

    @SearchFilter
    private Set<Integer> headHunterFeaturerIdList;

    @SearchFilter
    private Ternary hasDynamicMarkers;

    @SearchFilter
    private Set<UUID> breadCrumbsFaceIdList;

    @SearchFilter
    private Set<UUID> breadCrumbsFaceIdExcludeList;

    @SearchFilter
    private Set<UUID> pageFaceIdList;

    @SearchFilter
    private Set<UUID> pageFaceIdExcludeList;

    private HierarchySearch headHierarchyChildsForTwinClassSearch;
    private HierarchySearch headHierarchyParentsForTwinClassSearch;
    private HierarchySearch extendsHierarchyChildsForTwinClassSearch;
    private HierarchySearch extendsHierarchyParentsForTwinClassSearch;
    private TwinClassSearchEntity configuredSearch;
    private boolean emptySearch = false;

    public TwinClassSearch addOwnerTypeExclude() {
        ownerTypeExcludeList = CollectionUtils.safeAdd(ownerTypeExcludeList, OwnerType.SYSTEM);
        return this;
    }

    public TwinClassSearch addTwinClassId(UUID twinClassId, boolean exclude) {
        return SetUtils.safeAdd(this, twinClassId, exclude,
                this::getTwinClassIdList, this::setTwinClassIdList,
                this::getTwinClassIdExcludeList, this::setTwinClassIdExcludeList);
    }

    public TwinClassSearch addTwinClassId(Collection<UUID> twinClassIdSet, boolean exclude) {
        return SetUtils.safeAddAll(this, twinClassIdSet, exclude,
                this::getTwinClassIdList, this::setTwinClassIdList,
                this::getTwinClassIdExcludeList, this::setTwinClassIdExcludeList);
    }
}
