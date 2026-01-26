package org.twins.core.domain.search;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.math.LongRange;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.twinclass.TwinClassFieldSearchEntity;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;


@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinClassFieldSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Map<UUID, Boolean> twinClassIdMap;
    private Map<UUID, Boolean> twinClassIdExcludeMap;
    private Set<String> keyLikeList;
    private Set<String> keyNotLikeList;
    private Set<String> nameI18nLikeList;
    private Set<String> nameI18nNotLikeList;
    private Set<String> descriptionI18nLikeList;
    private Set<String> descriptionI18nNotLikeList;
    private Set<String> externalIdLikeList;
    private Set<String> externalIdNotLikeList;
    private Set<Integer> fieldTyperIdList;
    private Set<Integer> fieldTyperIdExcludeList;
    private Set<Integer> twinSorterIdList;
    private Set<Integer> twinSorterIdExcludeList;
    private LongRange orderRange;
    private Set<UUID> viewPermissionIdList;
    private Set<UUID> viewPermissionIdExcludeList;
    private Set<UUID> editPermissionIdList;
    private Set<UUID> editPermissionIdExcludeList;
    private Ternary required;
    private Ternary system;
    private Ternary dependentField;
    private Ternary hasDependentFields;
    private boolean excludeSystemFields = true;
    private boolean inactiveSearch = false;
    private TwinClassFieldSearchEntity configuredSearch;
    private FieldProjectionSearch fieldProjectionSearch;
    private Ternary projectionField;
    private Ternary hasProjectionFields;

    public TwinClassFieldSearch addTwinClassId(final UUID id, final boolean searchExtends, boolean exclude) {
        if (exclude) {
            if (twinClassIdExcludeMap == null) {
                twinClassIdExcludeMap = new HashMap<>();
            }
            twinClassIdExcludeMap.put(id, searchExtends);
        } else {
            if (twinClassIdMap == null) {
                twinClassIdMap = new HashMap<>();
            }
            twinClassIdMap.put(id, searchExtends);
        }
        return this;
    }

    public TwinClassFieldSearch addTwinClassId(final Collection<UUID> ids, final boolean searchExtends, boolean exclude) {
        if (exclude) {
            if (twinClassIdExcludeMap == null) {
                twinClassIdExcludeMap = new HashMap<>();
            }
            for (UUID id : ids) {
                twinClassIdExcludeMap.put(id, searchExtends);
            }
        } else {
            if (twinClassIdMap == null) {
                twinClassIdMap = new HashMap<>();
            }
            for (UUID id : ids) {
                twinClassIdMap.put(id, searchExtends);
            }
        }
        return this;
    }

    public TwinClassFieldSearch addId(final UUID id, boolean exclude) {
        if (exclude)
            idExcludeList = CollectionUtils.safeAdd(idExcludeList, id);
        else
            idList = CollectionUtils.safeAdd(idList, id);
        return this;
    }

    public TwinClassFieldSearch addId(final Collection<UUID> ids, boolean exclude) {
        if (exclude)
            idExcludeList = CollectionUtils.safeAdd(idExcludeList, ids);
        else
            idList = CollectionUtils.safeAdd(idList, ids);
        return this;
    }

    public static final ImmutableList<Pair<Function<TwinClassFieldSearch, Set>, BiConsumer<TwinClassFieldSearch, Set>>> SET_FIELDS = ImmutableList.of(
            Pair.of(TwinClassFieldSearch::getIdList, TwinClassFieldSearch::setIdList),
            Pair.of(TwinClassFieldSearch::getKeyLikeList, TwinClassFieldSearch::setKeyLikeList),
            Pair.of(TwinClassFieldSearch::getKeyNotLikeList, TwinClassFieldSearch::setKeyNotLikeList),
            Pair.of(TwinClassFieldSearch::getNameI18nLikeList, TwinClassFieldSearch::setNameI18nLikeList),
            Pair.of(TwinClassFieldSearch::getNameI18nNotLikeList, TwinClassFieldSearch::setNameI18nNotLikeList),
            Pair.of(TwinClassFieldSearch::getDescriptionI18nLikeList, TwinClassFieldSearch::setDescriptionI18nLikeList),
            Pair.of(TwinClassFieldSearch::getDescriptionI18nNotLikeList, TwinClassFieldSearch::setDescriptionI18nNotLikeList),
            Pair.of(TwinClassFieldSearch::getExternalIdLikeList, TwinClassFieldSearch::setExternalIdLikeList),
            Pair.of(TwinClassFieldSearch::getExternalIdNotLikeList, TwinClassFieldSearch::setExternalIdNotLikeList),
            Pair.of(TwinClassFieldSearch::getFieldTyperIdList, TwinClassFieldSearch::setFieldTyperIdList),
            Pair.of(TwinClassFieldSearch::getFieldTyperIdExcludeList, TwinClassFieldSearch::setFieldTyperIdExcludeList),
            Pair.of(TwinClassFieldSearch::getTwinSorterIdList, TwinClassFieldSearch::setTwinSorterIdList),
            Pair.of(TwinClassFieldSearch::getTwinSorterIdExcludeList, TwinClassFieldSearch::setTwinSorterIdExcludeList),
            Pair.of(TwinClassFieldSearch::getViewPermissionIdList, TwinClassFieldSearch::setViewPermissionIdList),
            Pair.of(TwinClassFieldSearch::getViewPermissionIdExcludeList, TwinClassFieldSearch::setViewPermissionIdExcludeList),
            Pair.of(TwinClassFieldSearch::getEditPermissionIdList, TwinClassFieldSearch::setEditPermissionIdList),
            Pair.of(TwinClassFieldSearch::getEditPermissionIdExcludeList, TwinClassFieldSearch::setEditPermissionIdExcludeList)
    );

    public static final ImmutableList<Pair<Function<TwinClassFieldSearch, Ternary>, BiConsumer<TwinClassFieldSearch, Ternary>>> TERNARY_FIELD = ImmutableList.of(
            Pair.of(TwinClassFieldSearch::getRequired, TwinClassFieldSearch::setRequired),
            Pair.of(TwinClassFieldSearch::getSystem, TwinClassFieldSearch::setSystem),
            Pair.of(TwinClassFieldSearch::getProjectionField, TwinClassFieldSearch::setProjectionField),
            Pair.of(TwinClassFieldSearch::getHasProjectionFields, TwinClassFieldSearch::setHasProjectionFields),
            Pair.of(TwinClassFieldSearch::getHasDependentFields, TwinClassFieldSearch::setHasDependentFields),
            Pair.of(TwinClassFieldSearch::getDependentField, TwinClassFieldSearch::setDependentField)
    );
}
