package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySmartService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.domain.twin.TwinGroupKeyKind;
import org.twins.core.domain.twin.TwinGroupedSearch;
import org.twins.core.domain.twin.TwinGroupedSearchResult;
import org.twins.core.domain.twin.TwinMemberSort;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.user.UserService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.toSortSpecification;

@Service
@Slf4j
@RequiredArgsConstructor
public class TwinGroupedSearchService {
    /**
     * A grouped response is the whole filtered tree. Above this many twins the request is refused
     * instead of returning a cut tree.
     */
    public static final int MAX_TWINS = 5_000;

    private final TwinSearchServiceV2 twinSearchServiceV2;
    private final TwinClassFieldService twinClassFieldService;
    private final FeaturerService featurerService;
    private final UserService userService;
    private final TwinService twinService;
    private final AuthService authService;

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    public TwinGroupedSearchResult search(TwinGroupedSearch request) throws ServiceException {
        if (request == null || request.getGroupFieldId() == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "groupFieldId is required");
        }
        UUID groupFieldId = request.getGroupFieldId();
        twinSearchServiceV2.resolveGroupProvider(groupFieldId);
        TwinEntity.BasicField basicField = TwinEntity.BasicField.convertOrNull(groupFieldId);
        TwinGroupKeyKind keyKind = keyKind(basicField);
        TwinGroupSort groupSort = request.getGroupSort() != null ? request.getGroupSort() : defaultSort(keyKind);
        if (isNameSort(groupSort) && !nameSortable(keyKind)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                    "NAME sort is not supported for field " + groupFieldId);
        }

        ApiUser apiUser = authService.getApiUser();
        BasicSearch search = request.getSearch() != null ? request.getSearch() : twinSearchServiceV2.emptySearch();
        Specification<TwinEntity> filter = twinSearchServiceV2.createFilterSpecification(search, apiUser.getDomainId(), apiUser.getLocale());
        // count(root) follows the same joins as the search. A permission join can inflate it,
        // so a legal selection may be refused. A partial tree is worse.
        Long total = twinSearchServiceV2.count(filter);
        if (total != null && total > MAX_TWINS) {
            log.warn("Grouped search refused: filter matches {} twins, limit is {}", total, MAX_TWINS);
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_INCORRECT,
                    "grouped search matches " + total + " twins, limit is " + MAX_TWINS);
        }

        List<TwinEntity> twins = List.of();
        if (total != null && total > 0) {
            Specification<TwinEntity> spec = filter.and(memberOrder(request.getMemberSorts(), apiUser.getLocale()));
            twins = twinSearchServiceV2.jpaSpecificationExecutor().findAll(spec);
        }

        TwinClassFieldEntity dynamicField = null;
        TwinFieldStorage storage = null;
        if (basicField == null && !twins.isEmpty()) {
            dynamicField = twinClassFieldService.findEntitySafe(groupFieldId);
            FieldTyper fieldTyper = featurerService.getFeaturer(dynamicField.getFieldTyperFeaturerId(), FieldTyper.class);
            storage = fieldTyper.getStorage(dynamicField);
            storage.load(new Kit<>(twins, TwinEntity::getId));
        }
        TwinClassFieldEntity field = dynamicField;
        TwinFieldStorage fieldStorage = storage;
        List<TwinGroup> groups = TwinGroupAssembler.assemble(twins, twin -> keysOf(twin, basicField, field, fieldStorage));
        boolean dataListOption = keyKind == TwinGroupKeyKind.FLAVOR || TwinGroupKeyValues.isDataList(fieldStorage);
        for (TwinGroup group : groups) {
            group.setKeyKind(keyKind)
                    .setGroupFieldId(groupFieldId)
                    .setDataListOption(dataListOption);
        }
        sortGroups(groups, groupSort, keyKind);
        return new TwinGroupedSearchResult()
                .setGroupFieldId(groupFieldId)
                .setKeyKind(keyKind)
                .setGroups(groups);
    }

    private List<Object> keysOf(TwinEntity twin, TwinEntity.BasicField basicField, TwinClassFieldEntity field, TwinFieldStorage storage) throws ServiceException {
        if (basicField != null) {
            return TwinGroupKeyValues.basic(twin, basicField);
        }
        return TwinGroupKeyValues.dynamic(twin, field, storage);
    }

    private void sortGroups(List<TwinGroup> groups, TwinGroupSort groupSort, TwinGroupKeyKind keyKind) throws ServiceException {
        Comparator<TwinGroup> order = switch (groupSort) {
            case NAME_ASC -> TwinGroupAssembler.names(loadNames(keyKind, groups), true);
            case NAME_DESC -> TwinGroupAssembler.names(loadNames(keyKind, groups), false);
            case COUNT_ASC -> TwinGroupAssembler.counts(true);
            case COUNT_DESC -> TwinGroupAssembler.counts(false);
            case KEY_ASC -> TwinGroupAssembler.keys(true);
            case KEY_DESC -> TwinGroupAssembler.keys(false);
        };
        groups.sort(order);
    }

    private Map<UUID, String> loadNames(TwinGroupKeyKind keyKind, List<TwinGroup> groups) throws ServiceException {
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
        if (keyKind == TwinGroupKeyKind.HEAD) {
            Kit<TwinEntity, UUID> heads = twinService.findEntities(ids,
                    EntitySmartService.ListFindMode.ifMissedIgnore,
                    EntitySmartService.ReadPermissionCheckMode.none,
                    EntitySmartService.EntityValidateMode.none);
            for (TwinEntity head : heads) {
                names.put(head.getId(), head.getName());
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

    private static TwinGroupSort defaultSort(TwinGroupKeyKind keyKind) {
        return nameSortable(keyKind) ? TwinGroupSort.NAME_ASC : TwinGroupSort.KEY_ASC;
    }

    private static boolean nameSortable(TwinGroupKeyKind keyKind) {
        return keyKind == TwinGroupKeyKind.ASSIGNER_USER
                || keyKind == TwinGroupKeyKind.OWNER_USER
                || keyKind == TwinGroupKeyKind.CREATOR_USER
                || keyKind == TwinGroupKeyKind.HEAD;
    }

    private static boolean isNameSort(TwinGroupSort groupSort) {
        return groupSort == TwinGroupSort.NAME_ASC || groupSort == TwinGroupSort.NAME_DESC;
    }
}
