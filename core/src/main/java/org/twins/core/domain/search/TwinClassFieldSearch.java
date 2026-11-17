package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.math.LongRange;
import org.cambium.common.util.SetUtils;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldSearchEntity;

import java.util.*;


@Data
@Accessors(chain = true)
@FieldNameConstants
@EqualsAndHashCode(callSuper = false)
public class TwinClassFieldSearch extends EntitySearch<TwinClassFieldEntity> implements ConfiguredSearchHolder<TwinClassFieldSearchEntity> {
    @SearchFilter
    private Set<UUID> idList;

    @SearchFilter
    private Set<UUID> idExcludeList;

    @SearchFilter
    private Map<UUID, Boolean> twinClassIdMap;

    @SearchFilter
    private Map<UUID, Boolean> twinClassIdExcludeMap;

    @SearchFilter
    private Set<String> keyLikeList;

    @SearchFilter
    private Set<String> keyNotLikeList;

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
    private Set<Integer> fieldTyperIdList;

    @SearchFilter
    private Set<Integer> fieldTyperIdExcludeList;

    @SearchFilter
    private Set<Integer> fieldInitiatorIdList;

    @SearchFilter
    private Set<Integer> fieldInitiatorIdExcludeList;

    @SearchFilter
    private Set<Integer> twinSorterIdList;

    @SearchFilter
    private Set<Integer> twinSorterIdExcludeList;

    @SearchFilter
    private Set<UUID> viewPermissionIdList;

    @SearchFilter
    private Set<UUID> viewPermissionIdExcludeList;

    @SearchFilter
    private Set<UUID> editPermissionIdList;

    @SearchFilter
    private Set<UUID> editPermissionIdExcludeList;

    @SearchFilter
    private Ternary required;

    @SearchFilter
    private Ternary system;

    @SearchFilter
    private Ternary inheritable;

    @SearchFilter
    private Ternary dependentField;

    @SearchFilter
    private Ternary hasDependentFields;

    @SearchFilter
    private Ternary projectionField;

    @SearchFilter
    private Ternary hasProjectionFields;

    private LongRange orderRange;
    private boolean excludeSystemFields = true;
    private boolean emptySearch = false;
    private TwinClassFieldSearchEntity configuredSearch;
    private FieldProjectionSearch fieldProjectionSearch;

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
        return SetUtils.safeAdd(this, id, exclude,
                this::getIdList, this::setIdList,
                this::getIdExcludeList, this::setIdExcludeList);
    }

    public TwinClassFieldSearch addId(final Collection<UUID> ids, boolean exclude) {
        return SetUtils.safeAddAll(this, ids, exclude,
                this::getIdList, this::setIdList,
                this::getIdExcludeList, this::setIdExcludeList);
    }
}
