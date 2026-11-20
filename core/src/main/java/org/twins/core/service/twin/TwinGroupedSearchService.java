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
import org.twins.core.dao.user.UserEntity;
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
        List<TwinTreeAssembler.Leaf> leaves = new ArrayList<>(twins.size());
        for (TwinEntity twin : twins) {
            leaves.add(new TwinTreeAssembler.Leaf(twin, levelKeys(twin, levels)));
        }
        TwinTreeAssembler.Assembly assembly = TwinTreeAssembler.assemble(leaves, levels);
        loadKeyEntities(assembly.all());
        sortTree(assembly.roots(), levels);
        return new TwinGroupedSearchResult().setGroups(assembly.roots());
    }

    private List<List<Object>> levelKeys(TwinEntity twin, List<TwinGroupLevel> levels) throws ServiceException {
        List<List<Object>> keys = new ArrayList<>(levels.size());
        for (TwinGroupLevel level : levels) {
            if (level.getBasicField() != null) {
                keys.add(TwinGroupKeyValues.basic(twin, level.getBasicField()));
            } else if (level.getStorage() instanceof TwinFieldStorageLink) {
                // link keys are the ids of the far twins carried by the FieldValueLink in the kit
                // (a twin of a class without the field has no value and groups under the empty key)
                keys.add(TwinGroupKeyValues.linkEnds(twin.getFieldValuesKit() == null ? null
                        : twin.getFieldValuesKit().get(level.getDynamicField().getId())));
            } else {
                keys.add(TwinGroupKeyValues.dynamic(twin, level.getDynamicField(), level.getStorage()));
            }
        }
        return keys;
    }

    private void sortTree(List<TwinGroup> roots, List<TwinGroupLevel> levels) {
        sortLevel(List.of(roots), 0, levels);
    }

    private void sortLevel(List<List<TwinGroup>> siblingLists, int levelIndex, List<TwinGroupLevel> levels) {
        if (levelIndex >= levels.size()) {
            return;
        }
        List<TwinGroup> atLevel = new ArrayList<>();
        for (List<TwinGroup> siblings : siblingLists) {
            if (siblings != null) {
                atLevel.addAll(siblings);
            }
        }
        if (atLevel.isEmpty()) {
            return;
        }
        Comparator<TwinGroup> comparator = groupOrder(levels.get(levelIndex));
        for (List<TwinGroup> siblings : siblingLists) {
            if (siblings != null && siblings.size() > 1) {
                siblings.sort(comparator);
            }
        }
        if (levelIndex == levels.size() - 1) {
            return;
        }
        List<List<TwinGroup>> children = new ArrayList<>();
        for (TwinGroup group : atLevel) {
            if (group.getGroups() != null) {
                children.add(group.getGroups());
            }
        }
        sortLevel(children, levelIndex + 1, levels);
    }

    private Comparator<TwinGroup> groupOrder(TwinGroupLevel level) {
        return switch (level.getSort()) {
            case NAME_ASC -> nameOrder(true);
            case NAME_DESC -> nameOrder(false);
            case COUNT_ASC -> TwinTreeAssembler.counts(true);
            case COUNT_DESC -> TwinTreeAssembler.counts(false);
            case KEY_ASC -> TwinTreeAssembler.keys(true);
            case KEY_DESC -> TwinTreeAssembler.keys(false);
        };
    }

    /**
     * NAME_* sorts read the name of the key entity attached by {@link #loadKeyEntities}: users, heads and
     * link ends are the only name-sortable keys (see {@link #nameSortable}). A missing entity sorts by the
     * raw key; the empty key is always last.
     */
    private static Comparator<TwinGroup> nameOrder(boolean ascending) {
        return (left, right) -> {
            if (left.getKey() == null || right.getKey() == null) {
                return nullsLast(left.getKey(), right.getKey());
            }
            int byName = compareText(keyName(left), keyName(right), ascending);
            if (byName != 0) {
                return byName;
            }
            return ((UUID) left.getKey()).compareTo((UUID) right.getKey());
        };
    }

    private static String keyName(TwinGroup group) {
        if (group.getKeyEntity() instanceof UserEntity user) {
            return user.getName();
        }
        if (group.getKeyEntity() instanceof TwinEntity twin) {
            return twin.getName();
        }
        return null;
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

    private static int compareText(String left, String right, boolean ascending) {
        if (left == null || right == null) {
            return nullsLast(left, right);
        }
        int compared = left.compareTo(right);
        return ascending ? compared : -compared;
    }

    private static int nullsLast(Object left, Object right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return 0;
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
        TwinFieldStorage storage = fieldTyper.getStorage(field);
        level
                .setDynamicField(field)
                .setStorage(storage)
                .setKeyType(dynamicKeyType(field, storage));
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
