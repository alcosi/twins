package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.PaginationUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.i18n.specifications.I18nSpecification;
import org.twins.core.dao.search.*;
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.DBUMembershipCheck;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.SearchByAlias;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.domain.search.TwinSort;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.twin.detector.SearchDetector;
import org.twins.core.featurer.twin.executor.TwinSearchExecutor;
import org.twins.core.featurer.twin.finder.TwinFinder;
import org.twins.core.featurer.twin.sorter.TwinSorter;
import org.twins.core.service.EntitySearchService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DBUService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.search.TwinSearchPredicateService;
import org.twins.core.service.search.TwinSearchSortService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.cambium.common.util.MapUtils.narrowMapOfSets;
import static org.cambium.common.util.SetUtils.narrowSet;
import static org.twins.core.dao.specifications.CommonSpecification.checkClassWithDomain;
import static org.twins.core.dao.specifications.CommonSpecification.toSortSpecification;
import static org.twins.core.dao.specifications.twin.TwinSpecification.*;

/**
 * Twin search service (merged ex TwinSearchService + TwinSearchServiceV2): the standard
 * {@link EntitySearchService} heir — sort and group fields are twinClassFieldIds, including the
 * system base-field pseudo ids — plus the saved-search execution pipeline that has no
 * configured-search counterpart (aliases, head search, pluggable {@link TwinSearchExecutor});
 * see docs/configured_search_architecture.md, "Accepted differences".
 *
 * <p>Standard part (free search/count): {@link #createFilterSpecification},
 * {@link #createSortSpecification} (basic fields, i18n joins and dynamic-field TwinSorter),
 * group counting via {@code twin_class_field} storages. Plain {@code count}/{@code exists}/
 * {@code countBatch} come from the base class.</p>
 *
 * <p>Twin-specific part: {@code twin_search}/{@code twin_search_predicate} expansion
 * ({@link #toBasicSearch}), effective sort resolution ({@link #resolveSorts}), alias detection,
 * and the explicit-{@link TwinSort} executions ({@link #findTwins(BasicSearch, List, SimplePagination)},
 * {@link #findTwinsUnaligned}) with the deterministic id tie-breaker. Search objects stay
 * sort-free: sorting is always an explicit execution parameter (the same object drives count
 * queries, where ORDER BY is meaningless).</p>
 */
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
public class TwinSearchService extends EntitySearchService<BasicSearch, TwinEntity, UUID, UUID> {
    private final TwinRepository twinRepository;
    private final UserGroupService userGroupService;
    private final TwinClassFieldService twinClassFieldService;
    private final PermissionService permissionService;
    private final DBUService dbuService;
    private final EntityManager entityManager;
    private final TwinSearchRepository twinSearchRepository;
    private final TwinSearchAliasRepository twinSearchAliasRepository;
    private final TwinSearchPredicateRepository twinSearchPredicateRepository;
    private final TwinSearchSortService twinSearchSortService;
    private final TwinSearchPredicateService twinSearchPredicateService;
    private final EntitySmartService entitySmartService;
    @Lazy
    private final FeaturerService featurerService;
    // Shadows the inherited @Autowired field: both predecessor services constructor-injected
    // AuthService lazily (featurers and search executors constructor-depend on this service),
    // and this exact injection shape is preserved on purpose.
    @Lazy
    private final AuthService authService;

    @Override
    public JpaSpecificationExecutor<TwinEntity> jpaSpecificationExecutor() {
        return twinRepository;
    }

    @Override
    public BasicSearch newSearch() {
        return new BasicSearch();
    }

    @Override
    protected Class<TwinEntity> entityClass() {
        return TwinEntity.class;
    }

    @Override
    protected TwinEntity newEntity() {
        return new TwinEntity();
    }

    @Override
    public Specification<TwinEntity> createFilterSpecification(BasicSearch search, UUID domainId, Locale locale) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroupsForCurrentUser();
        UUID businessAccountId = apiUser.getBusinessAccountId();
        UUID userId = apiUser.getUser().getId();
        //todo create filter by basicSearch.getExtendsTwinClassIdList()
        Specification<TwinEntity> specification = createTwinEntityBasicSearchSpecification(search, userId);

        if (permissionService.currentUserHasPermission(Permissions.DOMAIN_TWINS_VIEW_ALL) || !search.isCheckViewPermission()) {
            specification = specification
                    .and(checkClassWithDomain(search.getTwinClassIdList(), apiUser));
        } else {
            detectSystemClassSearchCheck(search);
            specification = specification
                    .and(checkPermissions(domainId, businessAccountId, userId, apiUser.getUser().getUserGroupsFootprint()))
                    .and(checkClass(search.getTwinClassIdList(), apiUser, search.getDbuMembershipCheck()));
        }

        //HEAD TWIN CHECK
        if (null != search.getHeadSearch() && !search.getHeadSearch().isEmpty())
            specification = specification.and(
                    checkHeadTwin(
                            createTwinEntityBasicSearchSpecification(search.getHeadSearch(), userId),
                            search.getHeadSearch()
                    ));

        //CHILDREN TWINS CHECK
        if (null != search.getChildrenSearch() && !search.getChildrenSearch().isEmpty())
            specification = specification.and(
                    checkChildrenTwins(
                            createTwinEntityBasicSearchSpecification(search.getChildrenSearch(), userId),
                            search.getChildrenSearch()
                    ));

        return specification;
    }

    @Override
    protected UUID defaultSortField() {
        return SystemIds.TwinClassField.Base.CREATED_AT;
    }

    @Override
    public Specification<TwinEntity> createSortSpecification(UUID twinClassFieldId, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (twinClassFieldId == null)
            return (root, query, cb) -> cb.conjunction();
        boolean ascending = sortDirection != SortDirection.DESC;

        var basicField = TwinEntity.BasicField.convertOrNull(twinClassFieldId);
        if (basicField != null) {
            switch (basicField) {
                case NAME:
                    return toSortSpecification(ascending, TwinEntity.Fields.name);
                case DESCRIPTION:
                    return toSortSpecification(ascending, TwinEntity.Fields.description);
                case CREATED_AT:
                    return toSortSpecification(ascending, TwinEntity.Fields.createdAt);
                case EXTERNAL_ID:
                    return toSortSpecification(ascending, TwinEntity.Fields.externalId);
                case SORT_ORDER:
                    return toSortSpecification(ascending, TwinEntity.Fields.sortOrder);
                case FLAVOR_DATA_LIST_OPTION_ID:
                    return I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinEntity.Fields.flavorDataListOptionSpecOnly, DataListOptionEntity.Fields.optionI18nTranslationsSpecOnly);
                case TWIN_CLASS_ID:
                    return I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinEntity.Fields.twinClass, TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);
                case TWIN_STATUS_ID:
                    return I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinEntity.Fields.twinStatus, TwinStatusEntity.Fields.nameI18nTranslationsSpecOnly);
                case OWNER_USER_ID:
                    return toSortSpecification(ascending, TwinEntity.Fields.ownerUser, UserEntity.Fields.name);
                case ASSIGNEE_USER_ID:
                    return toSortSpecification(ascending, TwinEntity.Fields.assignerUserSpecOnly, UserEntity.Fields.name);
                case CREATOR_USER_ID:
                    return toSortSpecification(ascending, TwinEntity.Fields.createdByUserSpecOnly, UserEntity.Fields.name);
                default:
                    throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "Field " + twinClassFieldId + " is not groupable");
            }
        }

        // Default: dynamic field via TwinSorter featurer
        TwinClassFieldEntity twinClassField = twinClassFieldService.findEntitySafe(twinClassFieldId);
        TwinSorter fieldSorter = featurerService.getFeaturer(twinClassField.getTwinSorterFeaturerId(), TwinSorter.class);
        var sortFn = fieldSorter.createSort(twinClassField.getTwinSorterParams(), twinClassField, toHibernateDirection(sortDirection));
        return sortFn.apply((root, query, cb) -> cb.conjunction());
    }

    @Override
    public String convertToEntityField(UUID twinClassFieldId) throws ServiceException {
        var basicField = TwinEntity.BasicField.convertOrNull(twinClassFieldId);
        if (basicField == TwinEntity.BasicField.HEAD_TWIN_ID
                || basicField == TwinEntity.BasicField.FLAVOR_DATA_LIST_OPTION_ID
                || basicField == TwinEntity.BasicField.TWIN_CLASS_ID
                || basicField == TwinEntity.BasicField.TWIN_STATUS_ID
                || basicField == TwinEntity.BasicField.OWNER_USER_ID
                || basicField == TwinEntity.BasicField.ASSIGNEE_USER_ID
                || basicField == TwinEntity.BasicField.CREATOR_USER_ID)
            return basicField.getName();
        throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "Field " + twinClassFieldId + " is not groupable");
    }

    @Override
    public void mapGroupedField(TwinEntity entity, UUID field, Object value) {
        if (field.equals(SystemIds.TwinClassField.Base.TWIN_CLASS_ID))
            entity.setTwinClassId((UUID) value);
        else if (field.equals(SystemIds.TwinClassField.Base.STATUS_ID))
            entity.setTwinStatusId((UUID) value);
        else if (field.equals(SystemIds.TwinClassField.Base.OWNER_USER_ID))
            entity.setOwnerUserId((UUID) value);
        else if (field.equals(SystemIds.TwinClassField.Base.FLAVOR_ID))
            entity.setFlavorDataListOptionId((UUID) value);
        else if (field.equals(SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID))
            entity.setAssignerUserId((UUID) value);
        else if (field.equals(SystemIds.TwinClassField.Base.CREATOR_USER_ID))
            entity.setCreatedByUserId((UUID) value);
        else if (field.equals(SystemIds.TwinClassField.Base.HEAD_ID))
            entity.setHeadTwinId((UUID) value);
        else {
            // dynamic twin class field — store the grouped value generically for the count DTO;
            // one entry per field, so several dynamic dimensions survive the round trip
            if (entity.getGroupedFieldValues() == null)
                entity.setGroupedFieldValues(new HashMap<>());
            entity.getGroupedFieldValues().put(field, value);
        }
    }

    private static org.hibernate.query.SortDirection toHibernateDirection(SortDirection direction) {
        return direction == SortDirection.DESC
                ? org.hibernate.query.SortDirection.DESCENDING
                : org.hibernate.query.SortDirection.ASCENDING;
    }

    protected void detectSystemClassSearchCheck(BasicSearch basicSearch) throws ServiceException {
        if (basicSearch.getDbuMembershipCheck() != null) {
            return;
        }
        if (CollectionUtils.isEmpty(basicSearch.getTwinClassIdList())) {
            basicSearch
                    .addTwinClassId(List.of(SystemIds.TwinClass.BUSINESS_ACCOUNT, SystemIds.TwinClass.USER), true)
                    .setDbuMembershipCheck(DBUMembershipCheck.BLOCKED);
            return;
        }
        DBUMembershipCheck detectedCheck = DBUMembershipCheck.BLOCKED;
        for (UUID twinClassId : basicSearch.getTwinClassIdList()) {
            detectedCheck = dbuService.detectSystemTwinsDBUMembershipCheck(twinClassId);
            if (detectedCheck != DBUMembershipCheck.BLOCKED && basicSearch.getTwinClassIdList().size() > 1) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_INCORRECT, "mixed search is not allowed");
            }
        }
        basicSearch.setDbuMembershipCheck(detectedCheck);
    }

    public Map<UUID, Long> countByGroupFields(BasicSearch basicSearch, TwinEntity.BasicField basicField) throws ServiceException {
        return countByGroupFields(basicSearch, basicField.getId());
    }

    /**
     * Provider path for twin grouping: basic system field -> direct root field (reusing the
     * {@link #convertToEntityField(UUID)} groupable whitelist); dynamic twin class field ->
     * {@link org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage#countGroup}. Provider order
     * matches the {@code groupFields} iteration (the row columns are read back in that order).
     */
    @Override
    protected List<GroupExpressionProvider<TwinEntity>> resolveGroupProviders(BasicSearch search, Set<UUID> groupFields) throws ServiceException {
        List<GroupExpressionProvider<TwinEntity>> providers = new ArrayList<>(groupFields.size());
        for (UUID fieldId : groupFields) {
            TwinEntity.BasicField basicField = TwinEntity.BasicField.convertOrNull(fieldId);
            if (basicField != null) {
                providers.add(GroupExpressionProvider.ofField(convertToEntityField(fieldId)));
            } else {
                providers.add(countGroup(twinClassFieldService.findEntitySafe(fieldId)));
            }
        }
        return providers;
    }

    private GroupExpressionProvider<TwinEntity> countGroup(TwinClassFieldEntity field) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyper.class);
        GroupExpressionProvider<TwinEntity> group = fieldTyper.getStorage(field).countGroup(field);
        if (group == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                    "Field " + field.getId() + " is not countable; " + field.logShort());
        }
        return group;
    }

    /**
     * Public facade over the contract filter method (kept under the historical name — many
     * cross-package callers, e.g. UserSearchService child-twin subqueries).
     */
    public Specification<TwinEntity> createTwinEntitySearchSpecification(BasicSearch basicSearch) throws ServiceException {
        return createFilterSpecification(basicSearch);
    }

    //***********************************************************************//
    //todo clarify about offset and multiplicity of size.                    //
    // because in the repository pagination is paginated                     //
    // of25 + sz10 = pg2 (21-30) can expect 26-35?                           //
    // of8 + s10 = pg0 (1-10) can expect 9-18?                               //
    // if it is a multiple, then of30 + sz10 = pg3 (31-40) - everything is ok//
    //***********************************************************************//

    public PaginationResult<TwinEntity> findTwins(BasicSearch basicSearch, SimplePagination pagination) throws ServiceException {
        return findTwins(basicSearch, null, pagination);
    }

    /**
     * Free search execution. {@code sorts} are explicit field-value sorts (via the TwinSorter
     * featurer of each twin class field); when empty, the pagination query sort plus the id
     * tie-breaker decide the order.
     */
    public PaginationResult<TwinEntity> findTwins(BasicSearch basicSearch, List<TwinSort> sorts, SimplePagination pagination) throws ServiceException {
        detectSystemClassSearchCheck(basicSearch);
        Specification<TwinEntity> spec = createTwinEntitySearchSpecification(basicSearch);
        spec = addSorting(sorts, pagination, spec);
        Page<TwinEntity> ret = twinRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    //todo THIS IS TEMPORAL SOLUTION FOR WORKING TWIN SEARCH V3
    // without sorting
    public PaginationResult<TwinEntity> findTwins(List<BasicSearch> basicSearches, SimplePagination pagination) throws ServiceException {
        if (basicSearches.size() == 1) {
            return findTwins(basicSearches.getFirst(), pagination);
        }
        Set<TwinEntity> alreadyLoaded = new LinkedHashSet<>();
        for (BasicSearch basicSearch : basicSearches) {
            detectSystemClassSearchCheck(basicSearch);
            Specification<TwinEntity> spec = createTwinEntitySearchSpecification(basicSearch);
            // no per-search sorting: the merged result is sorted in memory below (createdAt desc)
            List<TwinEntity> ret = twinRepository.findAll(spec);
            alreadyLoaded.addAll(ret);
        }
        pagination.setTotalElements(alreadyLoaded.size());
        List<TwinEntity> all = alreadyLoaded.stream()
                .sorted(Comparator.comparing(TwinEntity::getCreatedAt).reversed())
                .collect(Collectors.toList());
        PaginationUtils.validPagination(pagination);
        int offset = pagination.getOffset();
        int limit = pagination.getLimit();
        int fromIndex = Math.min(offset, all.size());
        int toIndex = Math.min(offset + limit, all.size());
        List<TwinEntity> pageContent = all.subList(fromIndex, toIndex);
        return PaginationUtils.convertInPaginationResult(pageContent, pagination);
    }

    public Map<String, Long> countTwinsBySearchAliasInBatch(Map<String, SearchByAlias> searchMap) throws ServiceException {
        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<String, SearchByAlias> entry : searchMap.entrySet()) {
            List<TwinSearchEntity> searchEntities = detectSearchesByAlias(entry.getKey());
            if (searchEntities.size() == 1)
                result.put(entry.getKey(), twinSearchExecutor(searchEntities.getFirst()).count(searchEntities.getFirst(), entry.getValue().getParams(), entry.getValue().getNarrow()));
            else
                result.put(entry.getKey(), count(getBasicSearchesByAlias(searchEntities, entry.getValue())));
        }
        return result;
    }

    private List<BasicSearch> getBasicSearchesByAlias(List<TwinSearchEntity> searchEntities, SearchByAlias searchByAlias) throws ServiceException {
        List<BasicSearch> basicSearches = new ArrayList<>();
        for (TwinSearchEntity twinSearchEntity : searchEntities)
            basicSearches.add(toBasicSearch(twinSearchEntity, searchByAlias.getParams(), searchByAlias.getNarrow()));
        return basicSearches;
    }

    /**
     * Expands the saved search into a {@link BasicSearch}: predicates only. Sorting is resolved
     * separately by {@link #resolveSorts} — the search object is sort-free (the same object drives
     * count queries, where ORDER BY is meaningless).
     */
    public BasicSearch toBasicSearch(TwinSearchEntity twinSearchEntity, Map<String, String> namedParamsMap, BasicSearch narrowSearch) throws ServiceException {
        BasicSearch basicSearch = new BasicSearch();
        twinSearchPredicateService.loadPredicates(twinSearchEntity);
        addPredicates(twinSearchEntity.getSearchPredicateKit().getList(), namedParamsMap, basicSearch, narrowSearch);
        if (twinSearchEntity.getHeadTwinSearchId() != null) {
            List<TwinSearchPredicateEntity> headSearchPredicates = twinSearchPredicateRepository.findByTwinSearchId(twinSearchEntity.getHeadTwinSearchId());
            if (CollectionUtils.isNotEmpty(headSearchPredicates) && basicSearch.getHeadSearch() == null)
                basicSearch.setHeadSearch(new TwinSearch());
            addPredicates(headSearchPredicates, namedParamsMap, basicSearch.getHeadSearch(), narrowSearch);
        }
        basicSearch.setConfiguredSearch(twinSearchEntity);
        return basicSearch;
    }

    /**
     * Effective sort list for one saved search execution: client-requested sorts beat the configured
     * sort kit ({@code twin_search_sort} rows). Null means "no explicit order" — the caller's
     * pagination sort and the id tie-breaker decide.
     */
    public List<TwinSort> resolveSorts(TwinSearchEntity twinSearchEntity, List<TwinSort> clientSorts) throws ServiceException {
        if (CollectionUtils.isNotEmpty(clientSorts))
            return clientSorts;
        twinSearchSortService.loadSorts(twinSearchEntity);
        if (KitUtils.isEmpty(twinSearchEntity.getSortKit()))
            return null;
        List<TwinSort> sorts = new ArrayList<>();
        for (TwinSearchSortEntity sortEntity : twinSearchEntity.getSortKit().getList())
            sorts.add(new TwinSort().setTwinClassFieldId(sortEntity.getTwinClassFieldId()).setDirection(sortEntity.getDirection()));
        return sorts;
    }

    public <GT> Map<GT, Long> countGroupBy(BasicSearch basicSearch, String groupFieldName) throws ServiceException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<TwinEntity> root = query.from(TwinEntity.class);
        Path<GT> groupField = root.get(groupFieldName);
        Specification<TwinEntity> spec = createTwinEntitySearchSpecification(basicSearch);
        query.multiselect(groupField, cb.count(root));
        query.where(spec.toPredicate(root, query, cb));
        query.groupBy(groupField);
        List<Object[]> results = entityManager.createQuery(query).getResultList();
        Map<GT, Long> resultMap = new HashMap<>();
        for (Object[] result : results) resultMap.put((GT) result[0], (Long) result[1]);
        return resultMap;
    }

    public List<TwinSearchEntity> detectSearchesByAlias(String searchAliasId) throws ServiceException {
        TwinSearchAliasEntity twinSearchAliasEntity = twinSearchAliasRepository.findByDomainIdAndAlias(authService.getApiUser().getDomainId(), searchAliasId);
        if (twinSearchAliasEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_ALIAS_UNKNOWN);
        List<TwinSearchEntity> twinSearchEntityList = twinSearchRepository.findByTwinSearchAliasId(twinSearchAliasEntity.getId());
        if (twinSearchEntityList == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_ALIAS_UNKNOWN);
        SearchDetector searchDetector = featurerService.getFeaturer(twinSearchAliasEntity.getTwinSearchDetectorFeaturerId(), SearchDetector.class);
        List<TwinSearchEntity> detectedSearches = searchDetector.detect(twinSearchAliasEntity, twinSearchEntityList);
        if (CollectionUtils.isEmpty(detectedSearches))
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "no searches detected");
        return detectedSearches; // hope we will get only single value here, otherwise we will get problems with sorting and pagination
    }

    public PaginationResult<TwinEntity> findTwins(SearchByAlias searchByAlias, SimplePagination pagination) throws ServiceException {
        return findTwins(searchByAlias.getAlias(), searchByAlias.getParams(), searchByAlias.getNarrow(), searchByAlias.getSorts(), pagination);
    }

    public PaginationResult<TwinEntity> findTwins(String searchAliasId, Map<String, String> namedParamsMap, BasicSearch searchNarrow, List<TwinSort> sorts, SimplePagination pagination) throws ServiceException {
        return findTwins(detectSearchesByAlias(searchAliasId), namedParamsMap, searchNarrow, sorts, pagination);
    }

    public PaginationResult<TwinEntity> findTwins(UUID searchId, Map<String, String> namedParamsMap, BasicSearch searchNarrow, List<TwinSort> sorts, SimplePagination pagination) throws ServiceException {
        if (SystemIds.TwinSearch.UNLIMITED.equals(searchId)) {
            // free search: no configured pipeline, client sorts go straight to the free execution
            if (searchNarrow == null)
                searchNarrow = new BasicSearch();
            return findTwins(searchNarrow, sorts, pagination);
        }
        return findTwins(entitySmartService.findById(searchId, twinSearchRepository, EntitySmartService.FindMode.ifEmptyThrows), namedParamsMap, searchNarrow, sorts, pagination);
    }

    /**
     * Ids of the twins matched by the saved search: single-column criteria query over the search
     * predicates — no entities, no pagination, no count, no narrow. The saved search's own
     * executor is not run (predicate search only). At most {@code limit} ids are returned; the
     * {@code limit + 1} trick detects truncation and warns (no extra count query).
     */
    public Set<UUID> findTwinIds(UUID searchId, Map<String, String> namedParamsMap, int limit) throws ServiceException {
        if (limit < 1)
            throw new ServiceException(ErrorCodeTwins.PAGINATION_LIMIT_ERROR);
        TwinSearchEntity search = entitySmartService.findById(searchId, twinSearchRepository, EntitySmartService.FindMode.ifEmptyThrows);
        BasicSearch basicSearch = toBasicSearch(search, namedParamsMap, null);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UUID> query = cb.createQuery(UUID.class);
        Root<TwinEntity> root = query.from(TwinEntity.class);
        query.select(root.get(TwinEntity.Fields.id)).where(createTwinEntitySearchSpecification(basicSearch).toPredicate(root, query, cb));
        List<UUID> ids = entityManager.createQuery(query).setMaxResults(limit + 1).getResultList();
        if (ids.size() > limit)
            log.warn("findTwinIds[{}] matched more than limit[{}] twins, capped - twins beyond the cap are missed", searchId, limit);
        return Set.copyOf(ids.subList(0, Math.min(ids.size(), limit)));
    }

    public PaginationResult<TwinEntity> findTwins(TwinSearchEntity twinSearchEntity, Map<String, String> namedParamsMap, BasicSearch searchNarrow, List<TwinSort> sorts, SimplePagination pagination) throws ServiceException {
        PaginationUtils.validPagination(pagination);
        return twinSearchExecutor(twinSearchEntity).execute(twinSearchEntity, namedParamsMap, searchNarrow, sorts, pagination);
    }

    public PaginationResult<TwinEntity> findTwins(List<TwinSearchEntity> searchEntities, Map<String, String> namedParamsMap, BasicSearch searchNarrow, List<TwinSort> sorts, SimplePagination pagination) throws ServiceException {
        if (searchEntities.size() == 1)
            return findTwins(searchEntities.getFirst(), namedParamsMap, searchNarrow, sorts, pagination);
        SearchByAlias searchByAlias = new SearchByAlias();
        searchByAlias.setParams(namedParamsMap);
        searchByAlias.setNarrow(searchNarrow);
        // sorts are not resolved per search: the multi-search path sorts the merged result in memory
        List<BasicSearch> basicSearches = getBasicSearchesByAlias(searchEntities, searchByAlias);
        return findTwins(basicSearches, pagination);
    }

    /**
     * Resolves the executor configured for the saved search. The recursion guard
     * ({@link org.twins.core.holder.TwinSearchRecursionGuard}) lives entirely inside
     * {@link TwinSearchExecutor} — callers never pass it.
     */
    private TwinSearchExecutor twinSearchExecutor(TwinSearchEntity search) throws ServiceException {
        return featurerService.getFeaturer(search.getTwinSearchExecutorFeaturerId(), TwinSearchExecutor.class);
    }

    public List<TwinSearchEntity> getSearchById(List<UUID> searchIds) throws ServiceException {
        Map<UUID, TwinSearchEntity> byId = new HashMap<>();
        for (TwinSearchEntity search : twinSearchRepository.findAllById(searchIds))
            byId.put(search.getId(), search);
        List<TwinSearchEntity> ordered = new ArrayList<>(searchIds.size());
        for (UUID searchId : searchIds) {
            TwinSearchEntity search = byId.get(searchId);
            if (search == null)
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "child search not found: " + searchId);
            ordered.add(search);
        }
        return ordered;
    }

    /**
     * Same query as {@link #findTwins(BasicSearch, List, SimplePagination)}, including its sort,
     * but the offset does not have to be a multiple of the limit.
     */
    public PaginationResult<TwinEntity> findTwinsUnaligned(BasicSearch basicSearch, List<TwinSort> sorts, SimplePagination pagination) throws ServiceException {
        if (pagination.getLimit() < 1)
            throw new ServiceException(ErrorCodeTwins.PAGINATION_LIMIT_ERROR);
        if (pagination.getOffset() < 0)
            throw new ServiceException(ErrorCodeTwins.PAGINATION_ERROR, "pagination offset cannot be negative");
        detectSystemClassSearchCheck(basicSearch);
        Specification<TwinEntity> spec = createTwinEntitySearchSpecification(basicSearch);
        spec = addSorting(sorts, pagination, spec);
        Sort sort = pagination.getSort();
        Pageable pageable = new OffsetLimitPageRequest(pagination.getOffset(), pagination.getLimit(), sort == null ? Sort.unsorted() : sort);
        Page<TwinEntity> ret = twinRepository.findAll(spec, pageable);
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    protected void addPredicates(List<TwinSearchPredicateEntity> searchPredicates, Map<String, String> namedParamsMap, TwinSearch mainSearch, TwinSearch narrowSearch) throws ServiceException {
        TwinFinder twinFinder = null;
        for (TwinSearchPredicateEntity mainSearchPredicate : searchPredicates) {
            twinFinder = featurerService.getFeaturer(mainSearchPredicate.getTwinFinderFeaturerId(), TwinFinder.class);
            twinFinder.concatSearch(mainSearchPredicate.getTwinFinderParams(), mainSearch, namedParamsMap);
        }
        narrowSearch(mainSearch, narrowSearch);
    }

    private Specification<TwinEntity> addSorting(List<TwinSort> sorts, SimplePagination pagination, Specification<TwinEntity> specification) throws ServiceException {
        Specification<TwinEntity> sortedSpecification = specification;
        boolean featurerSortApplied = false;
        if (CollectionUtils.isNotEmpty(sorts)) {
            twinClassFieldService.loadTwinClassFieldsForTwinSorts(sorts);
            for (TwinSort twinSort : sorts) {
                TwinClassFieldEntity twinClassField = twinSort.getTwinClassField();
                TwinSorter fieldSorter = featurerService.getFeaturer(twinClassField.getTwinSorterFeaturerId(), TwinSorter.class);
                var sortFunction = fieldSorter.createSort(twinClassField.getTwinSorterParams(), twinClassField, twinSort.getDirection());
                if (sortFunction != null) {
                    sortedSpecification = sortFunction.apply(sortedSpecification);
                    featurerSortApplied = true;
                    if (pagination != null)
                        pagination.setSort(null);
                }
            }
        }
        return addIdOrderTail(sortedSpecification, pagination, featurerSortApplied);
    }

    /**
     * Appends twin id as the last order key to make the total order deterministic: sort keys are not unique,
     * and without a tie breaker separate page requests may return duplicates/skips on equal keys.
     */
    private Specification<TwinEntity> addIdOrderTail(Specification<TwinEntity> specification, SimplePagination pagination, boolean featurerSortApplied) {
        if (pagination != null && !featurerSortApplied) {
            // pageable sort replaces Specification orders (QueryUtils.toOrders), so unless a featurer sorter
            // nulled pagination sort, the tie breaker must be appended to the pageable sort itself
            Sort sort = pagination.getSort() == null ? Sort.unsorted() : pagination.getSort();
            pagination.setSort(sort.and(Sort.by(Sort.Direction.ASC, TwinEntity.Fields.id)));
        }
        return specification.and((root, query, cb) -> {
            if (!query.getResultType().equals(Long.class)) { // count query, same guard as TwinSorterDateField
                List<Order> current = new ArrayList<>(query.getOrderList());
                current.add(cb.asc(root.get(TwinEntity.Fields.id))); // tie breaker must stay the last order key
                query.orderBy(current);
            }
            return cb.conjunction();
        });
    }

    protected void narrowSearch(TwinSearch mainSearch, TwinSearch narrowSearch) {
        if (narrowSearch == null)
            return;
        for (Pair<Function<TwinSearch, Set<UUID>>, BiConsumer<TwinSearch, Set<UUID>>> functionPair : TwinSearch.FUNCTIONS) {
            Set<UUID> mainSet = functionPair.getKey().apply(mainSearch);
            Set<UUID> narrowSet = functionPair.getKey().apply(narrowSearch);
            functionPair.getValue().accept(mainSearch, narrowSet(mainSet, narrowSet));
        }
        mainSearch.setTwinNameLikeList(narrowSet(mainSearch.getTwinNameLikeList(), narrowSearch.getTwinNameLikeList()));
        mainSearch.setDstLinksAnyOfList(narrowMapOfSets(mainSearch.getDstLinksAnyOfList(), narrowSearch.getDstLinksAnyOfList()));
        mainSearch.setDstLinksNoAnyOfList(narrowMapOfSets(mainSearch.getDstLinksNoAnyOfList(), narrowSearch.getDstLinksNoAnyOfList()));
        mainSearch.setSrcLinksAnyOfList(narrowMapOfSets(mainSearch.getSrcLinksAnyOfList(), narrowSearch.getSrcLinksAnyOfList()));
        mainSearch.setSrcLinksNoAnyOfList(narrowMapOfSets(mainSearch.getSrcLinksNoAnyOfList(), narrowSearch.getSrcLinksNoAnyOfList()));
        // todo add distinct and hierarchyChildrenSearch
    }
}
