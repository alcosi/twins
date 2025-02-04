package org.twins.core.service.twin;

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
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.SearchByAlias;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.search.criteriabuilder.SearchCriteriaBuilder;
import org.twins.core.featurer.search.detector.SearchDetector;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.user.UserGroupService;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.MapUtils.narrowMapOfSets;
import static org.cambium.common.util.PaginationUtils.sortType;
import static org.cambium.common.util.SetUtils.narrowSet;
import static org.springframework.data.jpa.domain.Specification.where;
import static org.twins.core.dao.specifications.AbstractTwinEntityBasicSearchSpecification.createTwinEntityBasicSearchSpecification;
import static org.twins.core.dao.specifications.CommonSpecification.checkFieldUuid;
import static org.twins.core.dao.specifications.CommonSpecification.checkPermissions;
import static org.twins.core.dao.specifications.twin.TwinSpecification.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TwinSearchService {
    private final EntityManager entityManager;
    private final TwinRepository twinRepository;
    private final UserGroupService userGroupService;
    private final SearchRepository searchRepository;
    private final SearchAliasRepository searchAliasRepository;
    private final SearchPredicateRepository searchPredicateRepository;
    private final PermissionService permissionService;
    @Lazy
    private final FeaturerService featurerService;
    @Lazy
    private final AuthService authService;
    private final EntitySmartService entitySmartService;


    private Specification<TwinEntity> createTwinEntitySearchSpecification(BasicSearch basicSearch) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroups(apiUser);
        UUID domainId = apiUser.getDomainId();
        UUID businessAccountId = apiUser.getBusinessAccountId();
        UUID userId = apiUser.getUser().getId();
        Set<UUID> userGroups = apiUser.getUserGroups();
        //todo create filter by basicSearch.getExtendsTwinClassIdList()
        Specification<TwinEntity> specification = where(createTwinEntityBasicSearchSpecification(basicSearch,userId));

        if (!permissionService.currentUserHasPermission(Permissions.DOMAIN_TWINS_VIEW_ALL)) {
            specification = specification
                    .and(checkPermissions(domainId, businessAccountId, userId, userGroups))
                    .and(checkClass(basicSearch.getTwinClassIdList(), apiUser));
        } else {
            specification = specification
                    .and(checkFieldUuid(apiUser.getDomainId(),TwinEntity.Fields.twinClass,TwinClassEntity.Fields.domainId))
                    .and(checkClassId(basicSearch.getTwinClassIdList()));
        }


        //HEAD TWIN CHECK
        if (null != basicSearch.getHeadSearch() && !basicSearch.getHeadSearch().isEmpty())
            specification = specification.and(
                checkHeadTwin(
                        createTwinEntityBasicSearchSpecification(basicSearch.getHeadSearch(),userId),
                        basicSearch.getHeadSearch()
                ));

        //CHILDREN TWINS CHECK
        if (null != basicSearch.getChildrenSearch() && !basicSearch.getChildrenSearch().isEmpty())
            specification = specification.and(
                checkChildrenTwins(
                        createTwinEntityBasicSearchSpecification(basicSearch.getChildrenSearch(),userId),
                        basicSearch.getChildrenSearch()
                ));

        return specification;
    }

    public List<TwinEntity> findTwins(BasicSearch basicSearch) throws ServiceException {
        List<TwinEntity> ret = twinRepository.findAll(createTwinEntitySearchSpecification(basicSearch), sortType(false, TwinEntity.Fields.createdAt));
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
        return findTwins(List.of(basicSearch), pagination);
    }

    public PaginationResult<TwinEntity> findTwins(List<BasicSearch> basicSearches, SimplePagination pagination) throws ServiceException {
        Specification<TwinEntity> spec = where(null);
        for (BasicSearch basicSearch : basicSearches)
            spec = spec.or(createTwinEntitySearchSpecification(basicSearch));
        Page<TwinEntity> ret = twinRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    public Long count(Specification<TwinEntity> spec) throws ServiceException {
        return twinRepository.count(spec);
    }

    public Long count(BasicSearch basicSearch) throws ServiceException {
        return count(createTwinEntitySearchSpecification(basicSearch));
    }

    public Long count(List<BasicSearch> basicSearches) throws ServiceException {
        Specification<TwinEntity> spec = where(null);
        for (BasicSearch basicSearch : basicSearches)
            spec = spec.or(createTwinEntitySearchSpecification(basicSearch));
        return count(spec);
    }

    public Map<String, Long> countTwinsBySearchAliasInBatch(Map<String, SearchByAlias> searchMap) throws ServiceException {
        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<String, SearchByAlias> entry : searchMap.entrySet()) {
            List<SearchEntity> searchEntities = detectSearchesByAlias(entry.getKey());
            result.put(entry.getKey(), count(getBasicSearchesByAlias(searchEntities, entry.getValue())));
        }
        return result;
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

    public List<SearchEntity> detectSearchesByAlias(String searchAliasId) throws ServiceException {
        SearchAliasEntity searchAliasEntity = searchAliasRepository.findByDomainIdAndAlias(authService.getApiUser().getDomainId(), searchAliasId);
        if (searchAliasEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_ALIAS_UNKNOWN);
        List<SearchEntity> searchEntityList = searchRepository.findBySearchAliasId(searchAliasEntity.getId());
        if (searchEntityList == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_ALIAS_UNKNOWN);
        SearchDetector searchDetector = featurerService.getFeaturer(searchAliasEntity.getSearchDetectorFeaturer(), SearchDetector.class);
        List<SearchEntity> detectedSearches = searchDetector.detect(searchAliasEntity, searchEntityList);
        if (CollectionUtils.isEmpty(detectedSearches))
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "no searches detected");
        return detectedSearches;
    }

    public PaginationResult<TwinEntity> findTwins(SearchByAlias searchByAlias, SimplePagination pagination) throws ServiceException {
        return findTwins(searchByAlias.getAlias(), searchByAlias.getParams(), searchByAlias.getNarrow(), pagination);
    }

    public PaginationResult<TwinEntity> findTwins(String searchAliasId, Map<String, String> namedParamsMap, BasicSearch searchNarrow, SimplePagination pagination) throws ServiceException {
        return findTwins(detectSearchesByAlias(searchAliasId), namedParamsMap, searchNarrow, pagination);
    }

    public PaginationResult<TwinEntity> findTwins(UUID searchId, Map<String, String> namedParamsMap, BasicSearch searchNarrow, SimplePagination pagination) throws ServiceException {
        return findTwins(entitySmartService.findById(searchId, searchRepository, EntitySmartService.FindMode.ifEmptyThrows), namedParamsMap, searchNarrow, pagination);
    }

    public PaginationResult<TwinEntity> findTwins(SearchEntity searchEntity, Map<String, String> namedParamsMap, BasicSearch searchNarrow, SimplePagination pagination) throws ServiceException {
        return findTwins(List.of(searchEntity), namedParamsMap, searchNarrow, pagination);
    }

    public PaginationResult<TwinEntity> findTwins(List<SearchEntity> searchEntities, Map<String, String> namedParamsMap, BasicSearch searchNarrow, SimplePagination pagination) throws ServiceException {
        SearchByAlias searchByAlias = new SearchByAlias();
        searchByAlias.setParams(namedParamsMap);
        searchByAlias.setNarrow(searchNarrow);
        List<BasicSearch> basicSearches = getBasicSearchesByAlias(searchEntities, searchByAlias);
        return findTwins(basicSearches, pagination);
    }

    private List<BasicSearch> getBasicSearchesByAlias(List<SearchEntity> searchEntities, SearchByAlias searchByAlias) throws ServiceException {
        List<BasicSearch> basicSearches = new ArrayList<>();
        for (SearchEntity searchEntity : searchEntities) {
            BasicSearch basicSearch = new BasicSearch();
            addPredicates(searchEntity.getSearchPredicateList(), searchByAlias.getParams(), basicSearch, searchByAlias.getNarrow());
            if (searchEntity.getHeadTwinSearchId() != null) {
                List<SearchPredicateEntity> headSearchPredicates = searchPredicateRepository.findBySearchId(searchEntity.getHeadTwinSearchId());
                if (CollectionUtils.isNotEmpty(headSearchPredicates) && basicSearch.getHeadSearch() == null)
                    basicSearch.setHeadSearch(new TwinSearch());
                addPredicates(headSearchPredicates, searchByAlias.getParams(), basicSearch.getHeadSearch(), searchByAlias.getNarrow().getHeadSearch());
            }
            if (searchEntity.getChildrenTwinSearchId() != null) {
                List<SearchPredicateEntity> childrenSearchPredicates = searchPredicateRepository.findBySearchId(searchEntity.getChildrenTwinSearchId());
                if (CollectionUtils.isNotEmpty(childrenSearchPredicates) && basicSearch.getChildrenSearch() == null)
                    basicSearch.setChildrenSearch(new TwinSearch());
                addPredicates(childrenSearchPredicates, searchByAlias.getParams(), basicSearch.getChildrenSearch(), searchByAlias.getNarrow().getChildrenSearch());
            }
            basicSearches.add(basicSearch);
        }
        return basicSearches;
    }

    protected void addPredicates(List<SearchPredicateEntity> searchPredicates, Map<String, String> namedParamsMap, TwinSearch mainSearch, TwinSearch narrowSearch) throws ServiceException {
        SearchCriteriaBuilder searchCriteriaBuilder = null;
        for (SearchPredicateEntity mainSearchPredicate : searchPredicates) {
            searchCriteriaBuilder = featurerService.getFeaturer(mainSearchPredicate.getSearchCriteriaBuilderFeaturer(), SearchCriteriaBuilder.class);
            searchCriteriaBuilder.concat(mainSearch, mainSearchPredicate, namedParamsMap);
        }
        narrowSearch(mainSearch, narrowSearch);
    }

    protected void narrowSearch(TwinSearch mainSearch, TwinSearch narrowSearch) {
        if (narrowSearch == null)
            return;
        for (Pair<Function<TwinSearch, Set<UUID>>, BiConsumer<TwinSearch, Set<UUID>>> functioPair : TwinSearch.FUNCTIONS) {
            Set<UUID> mainSet = functioPair.getKey().apply(mainSearch);
            Set<UUID> narrowSet = functioPair.getKey().apply(narrowSearch);
            functioPair.getValue().accept(mainSearch, narrowSet(mainSet, narrowSet));
        }
        mainSearch.setTwinNameLikeList(narrowSet(mainSearch.getTwinNameLikeList(), narrowSearch.getTwinNameLikeList()));
        mainSearch.setTwinNameNotLikeList(narrowSet(mainSearch.getTwinNameNotLikeList(), narrowSearch.getTwinNameNotLikeList()));
        mainSearch.setTwinDescriptionLikeList(narrowSet(mainSearch.getTwinDescriptionLikeList(), narrowSearch.getTwinDescriptionLikeList()));
        mainSearch.setTwinDescriptionNotLikeList(narrowSet(mainSearch.getTwinDescriptionNotLikeList(), narrowSearch.getTwinDescriptionNotLikeList()));
        mainSearch.setTouchList(narrowSet(mainSearch.getTouchList(), narrowSearch.getTouchList()));
        mainSearch.setTouchExcludeList(narrowSet(mainSearch.getTouchExcludeList(), narrowSearch.getTouchExcludeList()));
        mainSearch.setLinksAnyOfList(narrowMapOfSets(mainSearch.getLinksAnyOfList(), narrowSearch.getLinksAnyOfList()));
        mainSearch.setLinksNoAnyOfList(narrowMapOfSets(mainSearch.getLinksNoAnyOfList(), narrowSearch.getLinksNoAnyOfList()));
        mainSearch.setLinksAllOfList(narrowMapOfSets(mainSearch.getLinksAllOfList(), narrowSearch.getLinksAllOfList()));
        mainSearch.setLinksNoAllOfList(narrowMapOfSets(mainSearch.getLinksNoAllOfList(), narrowSearch.getLinksNoAllOfList()));
    }
}
