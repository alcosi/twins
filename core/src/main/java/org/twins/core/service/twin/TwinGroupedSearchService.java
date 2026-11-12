package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySmartService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.domain.twin.TwinGroupCriterion;
import org.twins.core.domain.twin.TwinGroupKeyKind;
import org.twins.core.domain.twin.TwinGroupedSearch;
import org.twins.core.domain.twin.TwinGroupedSearchResult;
import org.twins.core.domain.twin.TwinMemberSort;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.twin.TwinGroupCriterionType;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.enums.twin.TwinGroupValueType;
import org.twins.core.enums.twin.TwinLinkNodeEnd;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageBoolean;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTimestamp;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.user.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.toSortSpecification;

@Service
@RequiredArgsConstructor
public class TwinGroupedSearchService {
    private final TwinSearchServiceV2 twinSearchServiceV2;
    private final TwinClassFieldService twinClassFieldService;
    private final FeaturerService featurerService;
    private final UserService userService;
    private final TwinService twinService;
    private final LinkService linkService;
    private final TwinLinkRepository twinLinkRepository;
    private final AuthService authService;

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    public TwinGroupedSearchResult search(TwinGroupedSearch request) throws ServiceException {
        BasicSearch search = requireSingleClass(request == null ? null : request.getSearch());
        List<TwinGroupCriterion> criteria = resolveCriteria(request.getCriteria());

        ApiUser apiUser = authService.getApiUser();
        Specification<TwinEntity> filter = twinSearchServiceV2.createFilterSpecification(search, apiUser.getDomainId(), apiUser.getLocale());
        Specification<TwinEntity> ordered = filter.and(memberOrder(request.getMemberSorts(), apiUser.getLocale()));
        List<TwinEntity> twins = twinSearchServiceV2.jpaSpecificationExecutor().findAll(ordered);
        if (twins == null) {
            twins = List.of();
        }

        loadDynamicFields(twins, criteria);
        Map<TwinGroupCriterion, Map<UUID, List<Object>>> linkKeys = loadLinkKeys(twins, criteria);
        List<TwinTreeAssembler.Leaf> leaves = new ArrayList<>(twins.size());
        for (TwinEntity twin : twins) {
            leaves.add(new TwinTreeAssembler.Leaf(twin, levelKeys(twin, criteria, linkKeys)));
        }
        List<TwinGroup> groups = TwinTreeAssembler.assemble(leaves, criteria);
        sortTree(groups, 0, criteria);
        return new TwinGroupedSearchResult().setGroups(groups);
    }

    private List<List<Object>> levelKeys(TwinEntity twin,
                                         List<TwinGroupCriterion> criteria,
                                         Map<TwinGroupCriterion, Map<UUID, List<Object>>> linkKeys) throws ServiceException {
        List<List<Object>> levels = new ArrayList<>(criteria.size());
        for (TwinGroupCriterion criterion : criteria) {
            if (criterion.getType() == TwinGroupCriterionType.LINK) {
                List<Object> keys = linkKeys.getOrDefault(criterion, Map.of()).get(twin.getId());
                levels.add(keys == null || keys.isEmpty() ? Collections.singletonList(null) : keys);
            } else if (criterion.getBasicField() != null) {
                levels.add(TwinGroupKeyValues.basic(twin, criterion.getBasicField()));
            } else {
                levels.add(TwinGroupKeyValues.dynamic(twin, criterion.getDynamicField(), criterion.getStorage()));
            }
        }
        return levels;
    }

    private void loadDynamicFields(List<TwinEntity> twins, List<TwinGroupCriterion> criteria) throws ServiceException {
        if (twins.isEmpty()) {
            return;
        }
        Kit<TwinEntity, UUID> kit = new Kit<>(twins, TwinEntity::getId);
        Set<TwinFieldStorage> loaded = new HashSet<>();
        for (TwinGroupCriterion criterion : criteria) {
            TwinFieldStorage storage = criterion.getStorage();
            if (storage != null && loaded.add(storage) && !storage.isLoaded(twins.get(0))) {
                storage.load(kit);
            }
        }
    }

    private Map<TwinGroupCriterion, Map<UUID, List<Object>>> loadLinkKeys(List<TwinEntity> twins, List<TwinGroupCriterion> criteria) {
        List<TwinGroupCriterion> links = criteria.stream().filter(criterion -> criterion.getType() == TwinGroupCriterionType.LINK).toList();
        Map<TwinGroupCriterion, Map<UUID, List<Object>>> result = new HashMap<>();
        if (links.isEmpty() || twins.isEmpty()) {
            return result;
        }
        Set<UUID> twinIds = new LinkedHashSet<>();
        Set<UUID> linkIds = new LinkedHashSet<>();
        for (TwinEntity twin : twins) {
            twinIds.add(twin.getId());
        }
        for (TwinGroupCriterion link : links) {
            linkIds.add(link.getLinkId());
            result.put(link, new HashMap<>());
        }
        List<TwinLinkEntity> rows = twinLinkRepository.findByLinkIdInAndEndpointIn(linkIds, twinIds);
        for (TwinLinkEntity row : rows) {
            for (TwinGroupCriterion link : links) {
                if (!link.getLinkId().equals(row.getLinkId())) {
                    continue;
                }
                UUID leafId = link.getNodeEnd() == TwinLinkNodeEnd.DST ? row.getSrcTwinId() : row.getDstTwinId();
                UUID nodeId = link.getNodeEnd() == TwinLinkNodeEnd.DST ? row.getDstTwinId() : row.getSrcTwinId();
                if (!twinIds.contains(leafId)) {
                    continue;
                }
                result.get(link).computeIfAbsent(leafId, ignored -> new ArrayList<>());
                List<Object> nodes = result.get(link).get(leafId);
                if (!nodes.contains(nodeId)) {
                    nodes.add(nodeId);
                }
            }
        }
        return result;
    }

    private void sortTree(List<TwinGroup> groups, int level, List<TwinGroupCriterion> criteria) throws ServiceException {
        if (groups == null || groups.isEmpty()) {
            return;
        }
        TwinGroupCriterion criterion = criteria.get(level);
        groups.sort(order(criterion, groups));
        if (level < criteria.size() - 1) {
            for (TwinGroup group : groups) {
                sortTree(group.getGroups(), level + 1, criteria);
            }
        }
    }

    private Comparator<TwinGroup> order(TwinGroupCriterion criterion, List<TwinGroup> groups) throws ServiceException {
        return switch (criterion.getSort()) {
            case NAME_ASC -> TwinGroupAssembler.names(loadNames(criterion, groups), true);
            case NAME_DESC -> TwinGroupAssembler.names(loadNames(criterion, groups), false);
            case COUNT_ASC -> TwinGroupAssembler.counts(true);
            case COUNT_DESC -> TwinGroupAssembler.counts(false);
            case KEY_ASC -> TwinGroupAssembler.keys(true);
            case KEY_DESC -> TwinGroupAssembler.keys(false);
        };
    }

    private Map<UUID, String> loadNames(TwinGroupCriterion criterion, List<TwinGroup> groups) throws ServiceException {
        if (!isNameSort(criterion.getSort())) {
            return Map.of();
        }
        List<UUID> ids = new ArrayList<>();
        for (TwinGroup group : groups) {
            if (group.getKey() instanceof UUID id) {
                ids.add(id);
            }
        }
        Map<UUID, String> names = new HashMap<>();
        if (ids.isEmpty()) {
            return names;
        }
        if (criterion.getKeyKind() == TwinGroupKeyKind.HEAD || criterion.getKeyKind() == TwinGroupKeyKind.LINK) {
            Kit<TwinEntity, UUID> nodes = twinService.findEntities(ids,
                    EntitySmartService.ListFindMode.ifMissedIgnore,
                    EntitySmartService.ReadPermissionCheckMode.none,
                    EntitySmartService.EntityValidateMode.none);
            for (TwinEntity node : nodes) {
                names.put(node.getId(), node.getName());
            }
            return names;
        }
        Kit<UserEntity, UUID> users = userService.findEntities(ids,
                EntitySmartService.ListFindMode.ifMissedIgnore,
                EntitySmartService.ReadPermissionCheckMode.none,
                EntitySmartService.EntityValidateMode.none);
        for (UserEntity user : users) {
            names.put(user.getId(), user.getName());
        }
        return names;
    }

    private BasicSearch requireSingleClass(BasicSearch search) throws ServiceException {
        if (search == null || CollectionUtils.isEmpty(search.getTwinClassIdList()) || search.getTwinClassIdList().size() != 1) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "grouped search requires exactly one twin class");
        }
        return search;
    }

    private List<TwinGroupCriterion> resolveCriteria(List<TwinGroupCriterion> criteria) throws ServiceException {
        if (criteria == null || criteria.isEmpty() || criteria.size() > 3) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "criteria accepts from 1 to 3 items");
        }
        Set<String> seen = new HashSet<>();
        for (TwinGroupCriterion criterion : criteria) {
            if (criterion == null || criterion.getType() == null) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "criterion type is required");
            }
            if (criterion.getType() == TwinGroupCriterionType.LINK) {
                resolveLink(criterion, seen);
            } else {
                resolveField(criterion, seen);
            }
            if (criterion.getSort() == null) {
                criterion.setSort(defaultSort(criterion));
            }
            if (isNameSort(criterion.getSort()) && !nameSortable(criterion)) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                        "NAME sort is not supported for criterion " + criterion.getFieldId());
            }
        }
        return criteria;
    }

    private void resolveField(TwinGroupCriterion criterion, Set<String> seen) throws ServiceException {
        if (criterion.getFieldId() == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "fieldId is required");
        }
        if (!seen.add("FIELD:" + criterion.getFieldId())) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "duplicate group criterion " + criterion.getFieldId());
        }
        twinSearchServiceV2.resolveGroupProvider(criterion.getFieldId());
        TwinEntity.BasicField basicField = TwinEntity.BasicField.convertOrNull(criterion.getFieldId());
        TwinGroupKeyKind kind = keyKind(basicField);
        criterion.setBasicField(basicField).setKeyKind(kind);
        if (basicField == null) {
            TwinClassFieldEntity field = twinClassFieldService.findEntitySafe(criterion.getFieldId());
            FieldTyper fieldTyper = featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyper.class);
            TwinFieldStorage storage = fieldTyper.getStorage(field);
            boolean dataList = TwinGroupKeyValues.isDataList(storage);
            criterion.setDynamicField(field)
                    .setStorage(storage)
                    .setDataListOption(dataList)
                    .setValueType(dynamicValueType(storage, dataList));
        } else {
            if (basicField == TwinEntity.BasicField.FLAVOR_DATA_LIST_OPTION_ID) {
                criterion.setDataListOption(true);
            }
            criterion.setValueType(valueType(kind));
        }
    }

    private void resolveLink(TwinGroupCriterion criterion, Set<String> seen) throws ServiceException {
        if (criterion.getLinkId() == null || criterion.getNodeEnd() == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "linkId and nodeEnd are required");
        }
        if (!seen.add("LINK:" + criterion.getLinkId() + ":" + criterion.getNodeEnd())) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "duplicate group criterion " + criterion.getLinkId());
        }
        linkService.findEntitySafe(criterion.getLinkId());
        criterion.setKeyKind(TwinGroupKeyKind.LINK).setValueType(TwinGroupValueType.TWIN);
    }

    private Specification<TwinEntity> memberOrder(List<TwinMemberSort> memberSorts, Locale locale) throws ServiceException {
        List<TwinMemberSort> effective = memberSorts == null || memberSorts.isEmpty()
                ? List.of(new TwinMemberSort()
                .setFieldId(SystemIds.TwinClassField.Base.CREATED_AT)
                .setDirection(SortDirection.DESC))
                : memberSorts;
        if (effective.size() > 2) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "memberSorts accepts at most 2 fields");
        }
        Specification<TwinEntity> spec = (root, query, cb) -> cb.conjunction();
        for (TwinMemberSort sort : effective) {
            if (sort == null || sort.getFieldId() == null) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "memberSorts field is required");
            }
            if (TwinEntity.BasicField.convertOrNull(sort.getFieldId()) == null) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                        "member sort field " + sort.getFieldId() + " is not a basic field");
            }
            spec = spec.and(twinSearchServiceV2.createSortSpecification(sort.getFieldId(), sort.getDirection(), locale));
        }
        return spec.and(toSortSpecification(true, TwinEntity.Fields.id));
    }

    private static TwinGroupValueType valueType(TwinGroupKeyKind kind) {
        return switch (kind) {
            case ASSIGNER_USER, OWNER_USER, CREATOR_USER -> TwinGroupValueType.USER;
            case STATUS -> TwinGroupValueType.STATUS;
            case TWIN_CLASS -> TwinGroupValueType.TWIN_CLASS;
            case HEAD, LINK -> TwinGroupValueType.TWIN;
            case FLAVOR -> TwinGroupValueType.DATA_LIST_OPTION;
            case DYNAMIC -> TwinGroupValueType.TEXT;
        };
    }

    private static TwinGroupValueType dynamicValueType(TwinFieldStorage storage, boolean dataList) {
        if (dataList || storage instanceof TwinFieldStorageDatalist) {
            return TwinGroupValueType.DATA_LIST_OPTION;
        }
        if (storage instanceof TwinFieldStorageDecimal) {
            return TwinGroupValueType.DECIMAL;
        }
        if (storage instanceof TwinFieldStorageBoolean) {
            return TwinGroupValueType.BOOLEAN;
        }
        if (storage instanceof TwinFieldStorageTimestamp) {
            return TwinGroupValueType.TIMESTAMP;
        }
        return TwinGroupValueType.TEXT;
    }

    private static TwinGroupKeyKind keyKind(TwinEntity.BasicField basicField) throws ServiceException {
        if (basicField == null) {
            return TwinGroupKeyKind.DYNAMIC;
        }
        return switch (basicField) {
            case ASSIGNEE_USER_ID -> TwinGroupKeyKind.ASSIGNER_USER;
            case OWNER_USER_ID -> TwinGroupKeyKind.OWNER_USER;
            case CREATOR_USER_ID -> TwinGroupKeyKind.CREATOR_USER;
            case TWIN_STATUS_ID -> TwinGroupKeyKind.STATUS;
            case TWIN_CLASS_ID -> TwinGroupKeyKind.TWIN_CLASS;
            case HEAD_TWIN_ID -> TwinGroupKeyKind.HEAD;
            case FLAVOR_DATA_LIST_OPTION_ID -> TwinGroupKeyKind.FLAVOR;
            default -> throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                    "Field " + basicField.getId() + " is not groupable");
        };
    }

    private static TwinGroupSort defaultSort(TwinGroupCriterion criterion) {
        return nameSortable(criterion) ? TwinGroupSort.NAME_ASC : TwinGroupSort.KEY_ASC;
    }

    private static boolean nameSortable(TwinGroupCriterion criterion) {
        return criterion.getKeyKind() == TwinGroupKeyKind.ASSIGNER_USER
                || criterion.getKeyKind() == TwinGroupKeyKind.OWNER_USER
                || criterion.getKeyKind() == TwinGroupKeyKind.CREATOR_USER
                || criterion.getKeyKind() == TwinGroupKeyKind.HEAD
                || criterion.getKeyKind() == TwinGroupKeyKind.LINK;
    }

    private static boolean isNameSort(TwinGroupSort groupSort) {
        return groupSort == TwinGroupSort.NAME_ASC || groupSort == TwinGroupSort.NAME_DESC;
    }
}
