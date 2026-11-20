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
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.twin.*;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.twin.TwinGroupKeyType;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.*;
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

        // canonical batch load: every field value (FieldValueLink included) lands in twin.fieldValuesKit
        twinService.loadFieldsValues(twins);
        TwinTreeAssembler.Assembly assembly = TwinTreeAssembler.assemble(twins, levels);
        loadKeyEntities(assembly.all());
        TwinTreeAssembler.sort(assembly.roots(), levels);
        return new TwinGroupedSearchResult().setGroups(assembly.roots());
    }

    /**
     * Batch-loads the entity behind every entity-typed group key (user, status, class, twin, data list
     * option) and attaches it to the group: the mapper then only postpones what is already loaded and
     * never queries by id itself. Keys come from the fetched twins, so a missed or read-denied entity
     * means broken data and fails the request (findEntitiesSafe). Scalar keys (TEXT, DECIMAL, BOOLEAN,
     * TIMESTAMP) have no entity to load.
     */
    private void loadKeyEntities(List<TwinGroup> allGroups) throws ServiceException {
        Map<TwinGroupKeyType, List<TwinGroup>> byType = new EnumMap<>(TwinGroupKeyType.class);
        for (TwinGroup group : allGroups) {
            if (group.getKeyType() != null && group.getKey() instanceof UUID) {
                byType.computeIfAbsent(group.getKeyType(), keyType -> new ArrayList<>()).add(group);
            }
        }
        attachKeyEntities(byType.get(TwinGroupKeyType.USER), userService);
        attachKeyEntities(byType.get(TwinGroupKeyType.STATUS), twinStatusService);
        attachKeyEntities(byType.get(TwinGroupKeyType.TWIN_CLASS), twinClassService);
        attachKeyEntities(byType.get(TwinGroupKeyType.TWIN), twinService);
        attachKeyEntities(byType.get(TwinGroupKeyType.DATA_LIST_OPTION_FLAVOR), dataListOptionService);
        attachKeyEntities(byType.get(TwinGroupKeyType.DATA_LIST_OPTION_FIELD), dataListOptionService);
    }

    private static List<UUID> keyIds(List<TwinGroup> groups) {
        Set<UUID> ids = new LinkedHashSet<>();
        for (TwinGroup group : groups) {
            ids.add((UUID) group.getKey());
        }
        return new ArrayList<>(ids);
    }

    private static void attachKeyEntities(List<TwinGroup> groups, EntitySecureFindServiceImpl<?> entityService) throws ServiceException {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }
        Kit<?, UUID> kit = entityService.findEntitiesSafe(keyIds(groups));
        for (TwinGroup group : groups) {
            group.setKeyEntity(kit.get((UUID) group.getKey()));
        }
    }

    private List<TwinGroupLevel> resolveLevels(List<TwinGroupLevel> levels) throws ServiceException {
        // size (1..3) and non-null elements are bean-validated on TwinGroupedSearchRqDTOv1
        if (levels == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "levels are required");
        }
        Set<String> seen = new HashSet<>();
        List<UUID> dynamicFieldIds = new ArrayList<>();
        for (TwinGroupLevel level : levels) {
            if (!seen.add("FIELD:" + level.getFieldId())) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "duplicate group level " + level.getFieldId());
            }
            if (!SystemIdLookup.isSystemField(level.getFieldId()))
                dynamicFieldIds.add(level.getFieldId());
        }
        Kit<TwinClassFieldEntity, UUID> dynamicFields = loadFields(dynamicFieldIds);
        for (TwinGroupLevel level : levels) {
            if (SystemIdLookup.isSystemField(level.getFieldId())) {
                resolveBasicField(level);
            } else {
                resolveDynamicField(level, dynamicFields.get(level.getFieldId()));
            }
            if (level.getSort() == null) {
                level.setSort(defaultSort(level));
            }
            if (isNameSort(level.getSort()) && !nameSortable(level)) {
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

    private void resolveBasicField(TwinGroupLevel level) throws ServiceException {
        TwinEntity.BasicField basicField = TwinEntity.BasicField.convertOrNull(level.getFieldId());
        level
                .setBasicField(basicField)
                .setKeyType(basicKeyType(basicField));
    }

    private void resolveDynamicField(TwinGroupLevel level, TwinClassFieldEntity field) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyper.class);
        level
                .setDynamicField(field)
                .setKeyType(dynamicKeyType(field, fieldTyper.getStorage(field)));
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

    private static TwinGroupKeyType basicKeyType(TwinEntity.BasicField basicField) throws ServiceException {
        return switch (basicField) {
            case ASSIGNEE_USER_ID, OWNER_USER_ID, CREATOR_USER_ID -> TwinGroupKeyType.USER;
            case TWIN_STATUS_ID -> TwinGroupKeyType.STATUS;
            case TWIN_CLASS_ID -> TwinGroupKeyType.TWIN_CLASS;
            case HEAD_TWIN_ID -> TwinGroupKeyType.TWIN;
            case FLAVOR_DATA_LIST_OPTION_ID -> TwinGroupKeyType.DATA_LIST_OPTION_FLAVOR;
            default -> throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                    "Field " + basicField.getId() + " is not groupable");
        };
    }

    private static TwinGroupKeyType dynamicKeyType(TwinClassFieldEntity field, TwinFieldStorage storage) throws ServiceException {
        if (storage instanceof TwinFieldStorageLink) {
            // the key is the id of a far twin in the FieldValueLink; a backward link field has empty
            // values (backward loading is disabled platform-wide) and groups under the empty key
            return TwinGroupKeyType.TWIN;
        }
        if (storage instanceof TwinFieldStorageDatalist) {
            return TwinGroupKeyType.DATA_LIST_OPTION_FIELD;
        }
        if (storage instanceof TwinFieldStorageDecimal) {
            return TwinGroupKeyType.DECIMAL;
        }
        if (storage instanceof TwinFieldStorageBoolean) {
            return TwinGroupKeyType.BOOLEAN;
        }
        if (storage instanceof TwinFieldStorageTimestamp) {
            return TwinGroupKeyType.TIMESTAMP;
        }
        if (storage instanceof TwinFieldStorageSimple) {
            return TwinGroupKeyType.TEXT;
        }
        // the same acceptance set as the count/v1 countGroup contract: simple, decimal, boolean,
        // timestamp and datalist storages (link goes through its own branch above)
        throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                "{} is not countable", field.logNormal());
    }

    private static TwinGroupSort defaultSort(TwinGroupLevel level) {
        return nameSortable(level) ? TwinGroupSort.NAME_ASC : TwinGroupSort.KEY_ASC;
    }

    private static boolean nameSortable(TwinGroupLevel level) {
        return level.getKeyType() == TwinGroupKeyType.USER
                || level.getKeyType() == TwinGroupKeyType.TWIN;
    }

    private static boolean isNameSort(TwinGroupSort groupSort) {
        return groupSort == TwinGroupSort.NAME_ASC || groupSort == TwinGroupSort.NAME_DESC;
    }
}
