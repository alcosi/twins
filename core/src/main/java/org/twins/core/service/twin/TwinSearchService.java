package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.PaginationUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.search.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.DBUMembershipCheck;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.SearchByAlias;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.domain.search.TwinSort;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.twin.detector.SearchDetector;
import org.twins.core.featurer.twin.finder.TwinFinder;
import org.twins.core.featurer.twin.sorter.TwinSorter;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DBUService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.search.TwinSearchPredicateService;
import org.twins.core.service.search.TwinSearchSortService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.user.UserGroupService;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.cambium.common.util.MapUtils.narrowMapOfSets;
import static org.cambium.common.util.PaginationUtils.sortType;
import static org.cambium.common.util.SetUtils.narrowSet;
import static org.twins.core.dao.specifications.twin.TwinSpecification.*;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
public class TwinSearchService {
    private final EntityManager entityManager;
    private final TwinRepository twinRepository;
    private final UserGroupService userGroupService;
    private final TwinSearchRepository twinSearchRepository;
    private final TwinSearchAliasRepository twinSearchAliasRepository;
    private final TwinSearchPredicateRepository twinSearchPredicateRepository;
    private final TwinSearchSortService twinSearchSortService;
    private final TwinSearchPredicateService twinSearchPredicateService;
    private final TwinClassFieldService twinClassFieldService;
    private final PermissionService permissionService;
    @Lazy
    private final FeaturerService featurerService;
    @Lazy
    private final AuthService authService;
    private final EntitySmartService entitySmartService;
    private final DBUService dbuService;

    public Specification<TwinEntity> createTwinEntitySearchSpecification(BasicSearch basicSearch) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroupsForCurrentUser();
        UUID domainId = apiUser.getDomainId();
        UUID businessAccountId = apiUser.getBusinessAccountId();
        UUID userId = apiUser.getUser().getId();
        //todo create filter by basicSearch.getExtendsTwinClassIdList()
        Specification<TwinEntity> specification = createTwinEntityBasicSearchSpecification(basicSearch, userId);

        if (permissionService.currentUserHasPermission(Permissions.DOMAIN_TWINS_VIEW_ALL) || !basicSearch.isCheckViewPermission()) {
            specification = specification
                    .and(checkFieldUuid(apiUser.getDomainId(), TwinEntity.Fields.twinClass, TwinClassEntity.Fields.domainId))
                    .and(checkClassId(basicSearch.getTwinClassIdList()));
        } else {
            detectSystemClassSearchCheck(basicSearch);
            specification = specification
                    .and(checkPermissions(domainId, businessAccountId, userId, apiUser.getUser().getUserGroups().getIdSetSafe()))
                    .and(checkClass(basicSearch.getTwinClassIdList(), apiUser, basicSearch.getDbuMembershipCheck()));
        }


        //HEAD TWIN CHECK
        if (null != basicSearch.getHeadSearch() && !basicSearch.getHeadSearch().isEmpty())
            specification = specification.and(
                    checkHeadTwin(
                            createTwinEntityBasicSearchSpecification(basicSearch.getHeadSearch(), userId),
                            basicSearch.getHeadSearch()
                    ));

        //CHILDREN TWINS CHECK
        if (null != basicSearch.getChildrenSearch() && !basicSearch.getChildrenSearch().isEmpty())
            specification = specification.and(
                    checkChildrenTwins(
                            createTwinEntityBasicSearchSpecification(basicSearch.getChildrenSearch(), userId),
                            basicSearch.getChildrenSearch()
                    ));

        return specification;
    }

    private void detectSystemClassSearchCheck(BasicSearch basicSearch) throws ServiceException {
        if (basicSearch.getDbuMembershipCheck() != null) {
            return;
        }
        if (CollectionUtils.isEmpty(basicSearch.getTwinClassIdList())) {
            basicSearch
                    .addTwinClassId(List.of(SystemEntityService.TWIN_CLASS_BUSINESS_ACCOUNT, SystemEntityService.TWIN_CLASS_USER), true)
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

    public List<TwinEntity> findTwins(BasicSearch basicSearch) throws ServiceException {
        List<TwinEntity> ret = twinRepository.findAll(createTwinEntitySearchSpecification(basicSearch), sortType(false, TwinEntity.Fields.createdAt));
        //todo someone's responsibility for checking if we previously checked the user's domain and business account. Purely a log for control if something slips through?
        return ret;
    }

    public Set<TwinEntity> findTwinsSet(BasicSearch basicSearch) throws ServiceException {
        Set<TwinEntity> ret = new HashSet<>(twinRepository.findAll(createTwinEntitySearchSpecification(basicSearch), sortType(false, TwinEntity.Fields.createdAt)));
        //todo someone's responsibility for checking if we previously checked the user's domain and business account. Purely a log for control if something slips through?
        return ret;
    }

    public <T> List<T> findTwins(BasicSearch basicSearch, Class<T> projection) throws ServiceException {
        //https://github.com/spring-projects/spring-data-jpa/pull/430
        return twinRepository.findBy(createTwinEntitySearchSpecification(basicSearch), t -> t.as(projection).all());
    }

    //***********************************************************************//
    //todo clarify about offset and multiplicity of size.                    //
    // because in the repository pagination is paginated                     //
    // of25 + sz10 = pg2 (21-30) can expect 26-35?                           //
    // of8 + s10 = pg0 (1-10) can expect 9-18?                               //
    // if it is a multiple, then of30 + sz10 = pg3 (31-40) - everything is ok//
    //***********************************************************************//

    public PaginationResult<TwinEntity> findTwins(BasicSearch basicSearch, SimplePagination pagination) throws ServiceException {
        detectSystemClassSearchCheck(basicSearch);
        Specification<TwinEntity> spec = createTwinEntitySearchSpecification(basicSearch);
        spec = addSorting(basicSearch, pagination, spec);
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
            spec = addSorting(basicSearch, pagination, spec);
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

    public Long count(Specification<TwinEntity> spec) throws ServiceException {
        return twinRepository.count(spec);
    }

    public Long count(BasicSearch basicSearch) throws ServiceException {
        return count(createTwinEntitySearchSpecification(basicSearch));
    }

    public Long count(List<BasicSearch> basicSearches) throws ServiceException {
        Specification<TwinEntity> spec = (root, query, builder) -> builder.disjunction();
        for (BasicSearch basicSearch : basicSearches)
            spec = spec.or(createTwinEntitySearchSpecification(basicSearch));
        return count(spec);
    }

    public boolean exists(Specification<TwinEntity> spec) throws ServiceException {
        return twinRepository.exists(spec);
    }

    public boolean exists(BasicSearch basicSearch) throws ServiceException {
        return exists(createTwinEntitySearchSpecification(basicSearch));
    }

    public Map<String, Long> countTwinsBySearchAliasInBatch(Map<String, SearchByAlias> searchMap) throws ServiceException {
        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<String, SearchByAlias> entry : searchMap.entrySet()) {
            List<TwinSearchEntity> searchEntities = detectSearchesByAlias(entry.getKey());
            result.put(entry.getKey(), count(getBasicSearchesByAlias(searchEntities, entry.getValue())));
        }
        return result;
    }

    private List<BasicSearch> getBasicSearchesByAlias(List<TwinSearchEntity> searchEntities, SearchByAlias searchByAlias) throws ServiceException {
        List<BasicSearch> basicSearches = new ArrayList<>();
        for (TwinSearchEntity twinSearchEntity : searchEntities) {
            BasicSearch basicSearch = new BasicSearch();
            twinSearchPredicateService.loadPredicates(twinSearchEntity);
            twinSearchSortService.loadSorts(twinSearchEntity);
            // narrow applies to every search
            addPredicates(twinSearchEntity.getSearchPredicateKit().getList(), searchByAlias.getParams(), basicSearch, searchByAlias.getNarrow());
            // narrow sort overrides every search sort
            if (searchByAlias.getNarrow() != null && CollectionUtils.isNotEmpty(searchByAlias.getNarrow().getSorts())) {
                basicSearch.setSorts(searchByAlias.getNarrow().getSorts());
            } else if (CollectionUtils.isNotEmpty(twinSearchEntity.getSortKit())) {
                addSorts(twinSearchEntity, basicSearch);
            }
            if (twinSearchEntity.getHeadTwinSearchId() != null) {
                List<TwinSearchPredicateEntity> headSearchPredicates = twinSearchPredicateRepository.findByTwinSearchId(twinSearchEntity.getHeadTwinSearchId());
                if (CollectionUtils.isNotEmpty(headSearchPredicates) && basicSearch.getHeadSearch() == null)
                    basicSearch.setHeadSearch(new TwinSearch());
                addPredicates(headSearchPredicates, searchByAlias.getParams(), basicSearch.getHeadSearch(), searchByAlias.getNarrow());
            }
            basicSearch.setConfiguredSearch(twinSearchEntity);
            basicSearches.add(basicSearch);
        }
        return basicSearches;
    }

    public Map<String, Long> countTwinsInBatch(Map<String, BasicSearch> searchMap) throws ServiceException {
        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<String, BasicSearch> entry : searchMap.entrySet())
            result.put(entry.getKey(), count(entry.getValue()));
        return result;
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
        return findTwins(searchByAlias.getAlias(), searchByAlias.getParams(), searchByAlias.getNarrow(), pagination);
    }

    public PaginationResult<TwinEntity> findTwins(String searchAliasId, Map<String, String> namedParamsMap, BasicSearch searchNarrow, SimplePagination pagination) throws ServiceException {
        return findTwins(detectSearchesByAlias(searchAliasId), namedParamsMap, searchNarrow, pagination);
    }

    public PaginationResult<TwinEntity> findTwins(UUID searchId, Map<String, String> namedParamsMap, BasicSearch searchNarrow, SimplePagination pagination) throws ServiceException {
        if (SystemEntityService.TWIN_SEARCH_UNLIMITED.equals(searchId)) {
            return findTwins(searchNarrow, pagination);
        }
        return findTwins(entitySmartService.findById(searchId, twinSearchRepository, EntitySmartService.FindMode.ifEmptyThrows), namedParamsMap, searchNarrow, pagination);
    }

    public PaginationResult<TwinEntity> findTwins(TwinSearchEntity twinSearchEntity, Map<String, String> namedParamsMap, BasicSearch searchNarrow, SimplePagination pagination) throws ServiceException {
        return findTwins(List.of(twinSearchEntity), namedParamsMap, searchNarrow, pagination);
    }

    public PaginationResult<TwinEntity> findTwins(List<TwinSearchEntity> searchEntities, Map<String, String> namedParamsMap, BasicSearch searchNarrow, SimplePagination pagination) throws ServiceException {
        SearchByAlias searchByAlias = new SearchByAlias();
        searchByAlias.setParams(namedParamsMap);
        searchByAlias.setNarrow(searchNarrow);
        List<BasicSearch> basicSearches = getBasicSearchesByAlias(searchEntities, searchByAlias);
        return findTwins(basicSearches, pagination);
    }

    protected void addPredicates(List<TwinSearchPredicateEntity> searchPredicates, Map<String, String> namedParamsMap, TwinSearch mainSearch, TwinSearch narrowSearch) throws ServiceException {
        TwinFinder twinFinder = null;
        for (TwinSearchPredicateEntity mainSearchPredicate : searchPredicates) {
            twinFinder = featurerService.getFeaturer(mainSearchPredicate.getTwinFinderFeaturerId(), TwinFinder.class);
            twinFinder.concat(mainSearch, mainSearchPredicate, namedParamsMap);
        }
        narrowSearch(mainSearch, narrowSearch);
    }

    protected void addSorts(TwinSearchEntity searchEntity, BasicSearch basicSearch) throws ServiceException {
        List<TwinSort> sorts = new ArrayList<>();
        for (TwinSearchSortEntity twinSort : searchEntity.getSortKit().getList())
            sorts.add(new TwinSort().setTwinClassFieldId(twinSort.getTwinClassFieldId()).setDirection(twinSort.getDirection()));
        basicSearch.setSorts(sorts);
    }

    private Specification<TwinEntity> addSorting(BasicSearch search, SimplePagination pagination, Specification<TwinEntity> specification) throws ServiceException {
        if (CollectionUtils.isNotEmpty(search.getSorts())) {
            twinClassFieldService.loadTwinClassFieldsForTwinSorts(search.getSorts());
            for (TwinSort twinSort : search.getSorts()) {
                TwinClassFieldEntity twinClassField = twinSort.getTwinClassField();
                TwinSorter fieldSorter = featurerService.getFeaturer(twinClassField.getTwinSorterFeaturerId(), TwinSorter.class);
                var sortFunction = fieldSorter.createSort(twinClassField.getTwinSorterParams(), twinClassField, twinSort.getDirection());
                if (sortFunction != null) {
                    specification = sortFunction.apply(specification);
                    if (pagination != null)
                        pagination.setSort(null);
                }
            }
        }
        return specification;
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
    }
}
