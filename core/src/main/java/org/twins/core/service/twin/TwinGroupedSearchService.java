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
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
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
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.enums.twin.TwinGroupValueType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperLink;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageBoolean;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageLink;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTimestamp;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.twinlink.TwinLinkService;
import org.twins.core.service.user.UserService;

import java.util.ArrayList;
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
    public static final int MAX_TWINS = 5_000;

    private final TwinSearchServiceV2 twinSearchServiceV2;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassService twinClassService;
    private final FeaturerService featurerService;
    private final UserService userService;
    private final TwinService twinService;
    private final TwinLinkService twinLinkService;
    private final LinkService linkService;
    private final AuthService authService;

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    public TwinGroupedSearchResult search(TwinGroupedSearch request) throws ServiceException {
        BasicSearch search = requireSingleClass(request == null ? null : request.getSearch());
        UUID classId = search.getTwinClassIdList().iterator().next();
        TwinClassEntity twinClass = twinClassService.findEntitySafe(classId);
        List<TwinGroupCriterion> levels = resolveCriteria(request.getLevels(), twinClass);

        ApiUser apiUser = authService.getApiUser();
        Specification<TwinEntity> leavesOrder = memberOrder(request.getMemberSorts(), apiUser.getLocale());
        Specification<TwinEntity> filter = twinSearchServiceV2.createFilterSpecification(search, apiUser.getDomainId(), apiUser.getLocale());
        Long total = twinSearchServiceV2.count(filter);
        if (total != null && total > MAX_TWINS) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_INCORRECT,
                    "grouped search matches " + total + " twins, limit is " + MAX_TWINS);
        }
        List<TwinEntity> twins = total == null || total == 0
                ? List.of()
                : twinSearchServiceV2.jpaSpecificationExecutor().findAll(filter.and(leavesOrder));
        if (twins == null) {
            twins = List.of();
        }

        loadDynamicFields(twins, levels);
        List<TwinTreeAssembler.Leaf> leaves = new ArrayList<>(twins.size());
        for (TwinEntity twin : twins) {
            leaves.add(new TwinTreeAssembler.Leaf(twin, levelKeys(twin, levels)));
        }
        List<TwinGroup> groups = TwinTreeAssembler.assemble(leaves, levels);
        sortTree(groups, levels);
        return new TwinGroupedSearchResult().setGroups(groups);
    }

    private List<List<Object>> levelKeys(TwinEntity twin, List<TwinGroupCriterion> criteria) throws ServiceException {
        List<List<Object>> levels = new ArrayList<>(criteria.size());
        for (TwinGroupCriterion criterion : criteria) {
            if (criterion.getBasicField() != null) {
                levels.add(TwinGroupKeyValues.basic(twin, criterion.getBasicField()));
            } else if (criterion.getStorage() instanceof TwinFieldStorageLink) {
                levels.add(TwinGroupKeyValues.linkEnds(twin.getTwinLinks(), criterion.getResolvedLinkId(), criterion.isForwardLink()));
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
        // link criteria are loaded together with one targeted query filtered by the resolved link ids,
        // instead of TwinFieldStorageLink.load which fetches every forward link of every twin
        Set<UUID> linkIds = new LinkedHashSet<>();
        Set<TwinFieldStorage> considered = new HashSet<>();
        for (TwinGroupCriterion criterion : criteria) {
            TwinFieldStorage storage = criterion.getStorage();
            if (storage == null) {
                continue;
            }
            if (storage instanceof TwinFieldStorageLink) {
                if (criterion.getResolvedLinkId() != null) {
                    linkIds.add(criterion.getResolvedLinkId());
                }
                continue;
            }
            if (!considered.add(storage)) {
                continue;
            }
            List<TwinEntity> needLoad = new ArrayList<>();
            for (TwinEntity twin : twins) {
                if (!storage.isLoaded(twin)) {
                    needLoad.add(twin);
                }
            }
            if (!needLoad.isEmpty()) {
                storage.load(new Kit<>(needLoad, TwinEntity::getId));
            }
        }
        if (!linkIds.isEmpty()) {
            twinLinkService.loadTwinForwardLinksByLinkIds(twins, linkIds);
        }
    }

    private void sortTree(List<TwinGroup> roots, List<TwinGroupCriterion> criteria) throws ServiceException {
        sortLevel(List.of(roots), 0, criteria);
    }

    private void sortLevel(List<List<TwinGroup>> siblingLists, int level, List<TwinGroupCriterion> criteria) throws ServiceException {
        if (level >= criteria.size()) {
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
        Comparator<TwinGroup> comparator = order(criteria.get(level), atLevel);
        for (List<TwinGroup> siblings : siblingLists) {
            if (siblings != null && siblings.size() > 1) {
                siblings.sort(comparator);
            }
        }
        if (level == criteria.size() - 1) {
            return;
        }
        List<List<TwinGroup>> children = new ArrayList<>();
        for (TwinGroup group : atLevel) {
            if (group.getGroups() != null) {
                children.add(group.getGroups());
            }
        }
        sortLevel(children, level + 1, criteria);
    }

    private Comparator<TwinGroup> order(TwinGroupCriterion criterion, List<TwinGroup> groups) throws ServiceException {
        return switch (criterion.getSort()) {
            case NAME_ASC -> TwinTreeAssembler.names(loadNames(criterion, groups), true);
            case NAME_DESC -> TwinTreeAssembler.names(loadNames(criterion, groups), false);
            case COUNT_ASC -> TwinTreeAssembler.counts(true);
            case COUNT_DESC -> TwinTreeAssembler.counts(false);
            case KEY_ASC -> TwinTreeAssembler.keys(true);
            case KEY_DESC -> TwinTreeAssembler.keys(false);
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
        if (criterion.getValueType() == TwinGroupValueType.TWIN) {
            Kit<TwinEntity, UUID> nodes = twinService.findEntities(ids,
                    EntitySmartService.ListFindMode.ifMissedIgnore,
                    EntitySmartService.ReadPermissionCheckMode.ifDeniedLog,
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

    private List<TwinGroupCriterion> resolveCriteria(List<TwinGroupCriterion> criteria, TwinClassEntity twinClass) throws ServiceException {
        if (criteria == null || criteria.isEmpty() || criteria.size() > 3) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "levels accepts from 1 to 3 items");
        }
        Set<String> seen = new HashSet<>();
        for (TwinGroupCriterion criterion : criteria) {
            if (criterion == null) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "criterion is required");
            }
            resolveField(criterion, seen, twinClass);
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

    private void resolveField(TwinGroupCriterion criterion, Set<String> seen, TwinClassEntity twinClass) throws ServiceException {
        if (criterion.getFieldId() == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "fieldId is required");
        }
        if (!seen.add("FIELD:" + criterion.getFieldId())) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "duplicate group criterion " + criterion.getFieldId());
        }
        TwinEntity.BasicField basicField = TwinEntity.BasicField.convertOrNull(criterion.getFieldId());
        TwinGroupKeyKind kind = keyKind(basicField);
        criterion.setBasicField(basicField).setKeyKind(kind);
        if (basicField == null) {
            // the class check goes first: a foreign-class field must fail with its own message,
            // before any typer/link resolution round-trips
            TwinClassFieldEntity field = twinClassFieldService.findEntitySafe(criterion.getFieldId());
            if (!twinClassFieldService.isValidForClass(twinClass, field)) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                        "Field " + field.getId() + " does not belong to the searched class");
            }
            FieldTyper fieldTyper = featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyper.class);
            TwinFieldStorage storage = fieldTyper.getStorage(field);
            boolean dataList = TwinGroupKeyValues.isDataList(storage);
            criterion.setDynamicField(field)
                    .setStorage(storage)
                    .setDataListOption(dataList);
            if (storage instanceof TwinFieldStorageLink) {
                // the link is resolved exactly once here; a successful resolveLinkField is the countability
                // check for this storage, TwinFieldStorageLink.countGroup is never null
                if (!(fieldTyper instanceof FieldTyperLink linkTyper)) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                            "Field " + field.getId() + " has link storage but its typer is not FieldTyperLink");
                }
                resolveLinkField(criterion, field, linkTyper);
            } else {
                // non-link dynamic fields keep the count/v1 acceptance rule
                twinSearchServiceV2.resolveGroupProvider(criterion.getFieldId());
                criterion.setValueType(dynamicValueType(storage, dataList));
            }
        } else {
            // basic fields keep the count/v1 acceptance rule (groupable column whitelist)
            twinSearchServiceV2.resolveGroupProvider(criterion.getFieldId());
            if (basicField == TwinEntity.BasicField.FLAVOR_DATA_LIST_OPTION_ID) {
                criterion.setDataListOption(true);
            }
            criterion.setValueType(valueType(kind));
        }
    }

    private void resolveLinkField(TwinGroupCriterion criterion, TwinClassFieldEntity field, FieldTyperLink typer) throws ServiceException {
        UUID linkId = typer.getLinkId(field.getFieldTyperParams());
        twinClassFieldService.loadTwinClass(field);
        LinkEntity link = linkService.findEntitySafe(linkId);
        LinkService.LinkDirection direction = linkService.detectLinkDirection(link, field.getTwinClass());
        if (direction == LinkService.LinkDirection.invalid) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                    "Field " + field.getId() + " link direction cannot be detected");
        }
        if (direction == LinkService.LinkDirection.backward) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                    "Field " + field.getId() + " is a backward link and cannot be used for grouped search");
        }
        criterion.setResolvedLinkId(linkId)
                .setForwardLink(direction == LinkService.LinkDirection.forward)
                .setValueType(TwinGroupValueType.TWIN);
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
            if (!TwinSearchServiceV2.isBasicSortFieldSupported(sort.getFieldId())) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                        "member sort field " + sort.getFieldId() + " is not sortable");
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
            case HEAD -> TwinGroupValueType.TWIN;
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
        return criterion.getValueType() == TwinGroupValueType.USER
                || criterion.getValueType() == TwinGroupValueType.TWIN;
    }

    private static boolean isNameSort(TwinGroupSort groupSort) {
        return groupSort == TwinGroupSort.NAME_ASC || groupSort == TwinGroupSort.NAME_DESC;
    }
}
