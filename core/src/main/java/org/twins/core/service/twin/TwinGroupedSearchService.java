package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.twin.*;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.*;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.service.SystemIdLookup;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.twinstatus.TwinStatusService;
import org.twins.core.service.user.UserService;

import java.util.*;

import static org.twins.core.dao.specifications.CommonSpecification.toSortSpecification;

@Service
@RequiredArgsConstructor
public class TwinGroupedSearchService {
    public static final int MAX_TWINS = 5_000;

    private final TwinSearchServiceV2 twinSearchServiceV2;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassService twinClassService;
    private final FeaturerService featurerService;
    private final UserService userService;
    private final TwinService twinService;
    private final TwinFieldService twinFieldService;
    private final AuthService authService;
    private final TwinStatusService twinStatusService;
    private final DataListOptionService dataListOptionService;

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    public TwinGroupedSearchResult search(TwinGroupedSearch request) throws ServiceException {
        if (request == null || request.getSearch() == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "search is required");
        }
        // any mix of classes is fine: the search runs as-is, grouping reads keys from whatever twins match
        List<TwinGroupLevel> levels = resolveLevels(request.getLevels());

        ApiUser apiUser = authService.getApiUser();
        // built before findAllUnpaged: an invalid member sort must fail before the count
        Specification<TwinEntity> leavesOrder = memberOrder(request.getMemberSorts(), apiUser.getLocale());
        List<TwinEntity> twins = twinSearchServiceV2.findAllUnpaged(request.getSearch(), leavesOrder, MAX_TWINS);

        // canonical batch load: every dynamic field value (FieldValueLink included) lands in twin.fieldValuesKit
        twinService.loadFieldsValues(twins);
        TwinTreeAssembler.Assembly assembly = TwinTreeAssembler.assemble(twins, levels, twinFieldService::getFieldValue);
        materializeKeyValues(assembly.all());
        TwinTreeAssembler.sort(assembly.roots(), levels);
        return new TwinGroupedSearchResult().setGroups(assembly.roots());
    }

    /**
     * Bulk-materializes the id-only FieldValueReferenceSingle key values (base-field keys) into concrete
     * values carrying the loaded entity — one findEntitiesSafe per entity type per request, so no N+1.
     * Dynamic-field keys (link, datalist) already carry their entities from loadFieldsValues and need
     * no second load. Keys come from the fetched twins, so a missed or read-denied entity means broken
     * data and fails the request (findEntitiesSafe). Scalar keys (text, decimal, boolean, date) have
     * no entity to load.
     */
    private void materializeKeyValues(List<TwinGroup> allGroups) throws ServiceException {
        Map<Class<? extends FieldValue>, List<TwinGroup>> byValueType = new LinkedHashMap<>();
        for (TwinGroup group : allGroups) {
            if (group.getKeyValue() instanceof FieldValueReferenceSingle reference) {
                byValueType.computeIfAbsent(reference.getValueType(), type -> new ArrayList<>()).add(group);
            }
        }
        materialize(byValueType.get(FieldValueUserSingle.class), userService);
        materialize(byValueType.get(FieldValueStatus.class), twinStatusService);
        materialize(byValueType.get(FieldValueTwinClassSingle.class), twinClassService);
        materialize(byValueType.get(FieldValueLinkSingle.class), twinService);
        materialize(byValueType.get(FieldValueSelectSingle.class), dataListOptionService);
    }

    private static void materialize(List<TwinGroup> groups, EntitySecureFindServiceImpl<?> entityService) throws ServiceException {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }
        Set<UUID> ids = new LinkedHashSet<>();
        for (TwinGroup group : groups) {
            ids.add(((FieldValueReferenceSingle) group.getKeyValue()).getId());
        }
        Kit<?, UUID> kit = entityService.findEntitiesSafe(new ArrayList<>(ids));
        for (TwinGroup group : groups) {
            FieldValueReferenceSingle reference = (FieldValueReferenceSingle) group.getKeyValue();
            group.setKeyValue(concreteValue(reference, kit.get(reference.getId())));
        }
    }

    private static FieldValueSingle concreteValue(FieldValueReferenceSingle reference, Object entity) {
        TwinClassFieldEntity field = reference.getTwinClassField();
        Class<? extends FieldValue> type = reference.getValueType();
        if (type == FieldValueUserSingle.class) {
            return new FieldValueUserSingle(field).setValue((UserEntity) entity);
        }
        if (type == FieldValueStatus.class) {
            return new FieldValueStatus(field).setValue((TwinStatusEntity) entity);
        }
        if (type == FieldValueTwinClassSingle.class) {
            return new FieldValueTwinClassSingle(field).setValue((TwinClassEntity) entity);
        }
        if (type == FieldValueLinkSingle.class) {
            return new FieldValueLinkSingle(field).setValue((TwinEntity) entity);
        }
        if (type == FieldValueSelectSingle.class) {
            return new FieldValueSelectSingle(field).setValue((DataListOptionEntity) entity);
        }
        throw new IllegalStateException("Unsupported key value type " + type);
    }

    private List<TwinGroupLevel> resolveLevels(List<TwinGroupLevel> levels) throws ServiceException {
        // size (1..3) and non-null elements are bean-validated on TwinGroupedSearchRqDTOv1
        if (levels == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "levels are required");
        }
        Set<UUID> seen = new HashSet<>();
        List<UUID> fieldIds = new ArrayList<>();
        for (TwinGroupLevel level : levels) {
            if (!seen.add(level.getFieldId())) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "duplicate group level " + level.getFieldId());
            }
            fieldIds.add(level.getFieldId());
        }
        // base fields are regular twin_class_field rows of the global ancestor class: one load covers all levels
        Kit<TwinClassFieldEntity, UUID> fields = loadFields(fieldIds);
        for (TwinGroupLevel level : levels) {
            boolean nameSortable = SystemIdLookup.isSystemField(level.getFieldId())
                    ? resolveBasicField(level, fields.get(level.getFieldId()))
                    : resolveDynamicField(level, fields.get(level.getFieldId()));
            if (level.getSort() == null) {
                level.setSort(nameSortable ? TwinGroupSort.NAME_ASC : TwinGroupSort.KEY_ASC);
            }
            if (isNameSort(level.getSort()) && !nameSortable) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                        "NAME sort is not supported for level " + level.getFieldId());
            }
        }
        return levels;
    }

    private Kit<TwinClassFieldEntity, UUID> loadFields(List<UUID> fieldIds) throws ServiceException {
        if (fieldIds.isEmpty()) {
            return Kit.emptyKit();
        }
        return twinClassFieldService.findEntitiesSafe(fieldIds);
    }

    /**
     * Base levels need no value shaping of their own: TwinFieldService.getFieldValue reads the twin
     * column (and an attached entity) by the field id. Returns whether the key has a name
     * (users and the head twin).
     */
    private boolean resolveBasicField(TwinGroupLevel level, TwinClassFieldEntity field) throws ServiceException {
        if (!TwinFieldService.GROUPABLE_BASIC_FIELDS.contains(TwinEntity.BasicField.convertOrNull(level.getFieldId()))) {
            // tags, markers and aliases are system fields but have no scalar column to group by
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                    "Field " + level.getFieldId() + " is not groupable");
        }
        level.setField(field);
        return NAME_SORTABLE_BASIC_FIELDS.contains(TwinEntity.BasicField.convertOrNull(level.getFieldId()));
    }

    /**
     * Dynamic levels are validated against the same acceptance set as the count/v1 countGroup
     * contract: link, datalist, simple, decimal, boolean and timestamp storages. Returns whether
     * the key has a name (a link key is a twin).
     */
    private boolean resolveDynamicField(TwinGroupLevel level, TwinClassFieldEntity field) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyper.class);
        TwinFieldStorage storage = fieldTyper.getStorage(field);
        if (!(storage instanceof TwinFieldStorageLink
                || storage instanceof TwinFieldStorageDatalist
                || storage instanceof TwinFieldStorageTimestamp
                || storage instanceof TwinFieldStorageBoolean
                || storage instanceof TwinFieldStorageDecimal
                || storage instanceof TwinFieldStorageSimple)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                    "{} is not countable", field.logNormal());
        }
        level.setField(field);
        return storage instanceof TwinFieldStorageLink;
    }

    /** Base keys whose entity has a name: users and the head twin (statuses, classes and options do not). */
    private static final Set<TwinEntity.BasicField> NAME_SORTABLE_BASIC_FIELDS = Set.of(
            TwinEntity.BasicField.OWNER_USER_ID,
            TwinEntity.BasicField.ASSIGNEE_USER_ID,
            TwinEntity.BasicField.CREATOR_USER_ID,
            TwinEntity.BasicField.HEAD_TWIN_ID);

    private static boolean isNameSort(TwinGroupSort groupSort) {
        return groupSort == TwinGroupSort.NAME_ASC || groupSort == TwinGroupSort.NAME_DESC;
    }

    private Specification<TwinEntity> memberOrder(List<TwinMemberSort> memberSorts, Locale locale) throws ServiceException {
        // at most 2 non-null elements — bean-validated on TwinGroupedSearchRqDTOv1
        List<TwinMemberSort> effective = memberSorts == null || memberSorts.isEmpty()
                ? List.of(new TwinMemberSort()
                .setFieldId(SystemIds.TwinClassField.Base.CREATED_AT)
                .setDirection(SortDirection.DESC))
                : memberSorts;
        Specification<TwinEntity> spec = (root, query, cb) -> cb.conjunction();
        for (TwinMemberSort sort : effective) {
            if (sort.getFieldId() == null) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "memberSorts field is required");
            }
            if (!TwinSearchServiceV2.isBasicSortFieldSupported(sort.getFieldId())) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                        "member sort field " + sort.getFieldId() + " is not sortable");
            }
            spec = spec.and(twinSearchServiceV2.createSortSpecification(sort.getFieldId(), sort.getDirection(), locale));
        }
        return spec.and(toSortSpecification(true, TwinEntity.Fields.id));
    }
}
