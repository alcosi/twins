package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.PaginationUtils;
import org.cambium.featurer.FeaturerService;
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
import org.twins.core.domain.search.SearchAlias;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.search.criteriabuilder.SearchCriteriaBuilder;
import org.twins.core.featurer.search.detector.SearchDetector;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.user.UserGroupService;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.MapUtils.narrowMapOfSets;
import static org.cambium.common.util.PaginationUtils.sort;
import static org.cambium.common.util.SetUtils.narrowSet;
import static org.springframework.data.jpa.domain.Specification.where;
import static org.twins.core.dao.specifications.twin.TwinSpecification.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TwinSearchService {
    final EntityManager entityManager;
    final TwinRepository twinRepository;
    final TwinService twinService;
    final UserGroupService userGroupService;
    final SearchRepository searchRepository;
    final SearchAliasRepository searchAliasRepository;
    final SearchPredicateRepository searchPredicateRepository;
    final PermissionService permissionService;
    @Lazy
    final FeaturerService featurerService;
    @Lazy
    final AuthService authService;
    final EntitySmartService entitySmartService;

    private Specification<TwinEntity> createTwinEntityBasicSearchSpecification(TwinSearch twinSearch) throws ServiceException {

        return where(
                checkTwinLinks(twinSearch.getTwinLinksMap(), twinSearch.getTwinNoLinksMap())
                        .and(checkUuidIn(TwinEntity.Fields.id, twinSearch.getTwinIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.id, twinSearch.getTwinIdExcludeList(), true))
                        .and(checkFieldLikeIn(TwinEntity.Fields.name, twinSearch.getTwinNameLikeList(), true))
                        .and(checkUuidIn(TwinEntity.Fields.assignerUserId, twinSearch.getAssigneeUserIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.assignerUserId, twinSearch.getAssigneeUserIdExcludeList(), true))
                        .and(checkUuidIn(TwinEntity.Fields.createdByUserId, twinSearch.getCreatedByUserIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.createdByUserId, twinSearch.getCreatedByUserIdExcludeList(), true))
                        .and(checkUuidIn(TwinEntity.Fields.twinStatusId, twinSearch.getStatusIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.headTwinId, twinSearch.getHeaderTwinIdList(), false))
                        .and(checkHierarchyContainsAny(TwinEntity.Fields.hierarchyTree, twinSearch.getHierarchyTreeContainsIdList()))
                        .and(checkUuidIn(TwinEntity.Fields.twinStatusId, twinSearch.getStatusIdExcludeList(), true))
                        .and(checkUuidIn(TwinEntity.Fields.twinClassId, twinSearch.getTwinClassIdExcludeList(), true))
                        .and(checkTagIds(twinSearch.getTagDataListOptionIdList(), false))
                        .and(checkTagIds(twinSearch.getTagDataListOptionIdExcludeList(), true))
                        .and(checkMarkerIds(twinSearch.getMarkerDataListOptionIdList(), false))
                        .and(checkMarkerIds(twinSearch.getMarkerDataListOptionIdExcludeList(), true))
                        .and(checkTwinClassUuidFieldIn(TwinClassEntity.Fields.headTwinClassId, twinSearch.getHeadTwinClassIdList()))
                        .and(checkTwinClassUuidFieldIn(TwinClassEntity.Fields.extendsTwinClassId, twinSearch.getExtendsTwinClassIdList()))
        );
    }

    private Specification<TwinEntity> createTwinEntitySearchSpecification(BasicSearch basicSearch) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroups(apiUser);
        UUID domainId = apiUser.getDomainId();
        UUID businessAccountId = apiUser.getBusinessAccountId();
        UUID userId = apiUser.getUser().getId();
        Set<UUID> userGroups = apiUser.getUserGroups();
        //todo create filter by basicSearch.getExtendsTwinClassIdList()
        Specification<TwinEntity> specification = where(checkClass(basicSearch.getTwinClassIdList(), apiUser)
                .and(checkPermissions(domainId, businessAccountId, userId, userGroups))
                .and(createTwinEntityBasicSearchSpecification(basicSearch))
        );

        //HEAD TWIN CHECK
        if (null != basicSearch.getHeadSearch()) specification = specification.and(
                checkHeadTwin(
                        createTwinEntityBasicSearchSpecification(basicSearch.getHeadSearch()),
                        basicSearch.getHeadSearch()
                ));


        return specification;
    }

    public List<TwinEntity> findTwins(BasicSearch basicSearch) throws ServiceException {
        List<TwinEntity> ret = twinRepository.findAll(createTwinEntitySearchSpecification(basicSearch), sort(false, TwinEntity.Fields.createdAt));
        //todo someone's responsibility for checking if we previously checked the user's domain and business account. Purely a log for control if something slips through?
        return ret.stream().filter(t -> !twinService.isEntityReadDenied(t)).toList();
    }

    //***********************************************************************//
    //todo clarify about offset and multiplicity of size.                    //
    // because in the repository pagination is paginated                     //
    // of25 + sz10 = pg2 (21-30) can expect 26-35?                           //
    // of8 + s10 = pg0 (1-10) can expect 9-18?                               //
    // if it is a multiple, then of30 + sz10 = pg3 (31-40) - everything is ok//
    //***********************************************************************//

    public TwinSearchResult findTwins(BasicSearch basicSearch, int offset, int limit) throws ServiceException {
        return findTwins(List.of(basicSearch), offset, limit);
    }

    public TwinSearchResult findTwins(List<BasicSearch> basicSearches, int offset, int limit) throws ServiceException {
        Specification<TwinEntity> spec = where(null);
        for (BasicSearch basicSearch : basicSearches)
            spec = spec.or(createTwinEntitySearchSpecification(basicSearch));
        Page<TwinEntity> ret = twinRepository.findAll(spec, PaginationUtils.paginationOffset(offset, limit, sort(false, TwinEntity.Fields.createdAt)));
        return convertPageInTwinSearchResult(ret, offset, limit);
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

    public Map<String, Long> countTwinsBySearchAliasInBatch(Map<String, SearchAlias> searchMap) throws ServiceException {
        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<String, SearchAlias> entry : searchMap.entrySet()) {
            List<SearchEntity> searchEntities = detectSearchesByAlias(entry.getKey());
            result.put(entry.getKey(), count(getBasicSearchesByAlias(searchEntities, entry.getValue())));
        }
        return result;
    }

    private List<BasicSearch> getBasicSearchesByAlias(List<SearchEntity> searchEntities, SearchAlias searchAlias) throws ServiceException {
        List<BasicSearch> basicSearches = new ArrayList<>();
        for (SearchEntity searchEntity : searchEntities) {
            BasicSearch basicSearch = new BasicSearch();
            addPredicates(searchEntity.getSearchPredicateList(), searchAlias.getParams(), basicSearch, searchAlias.getNarrow());
            if (searchEntity.getHeadTwinSearchId() != null) {
                List<SearchPredicateEntity> headSearchPredicates = searchPredicateRepository.findBySearchId(searchEntity.getHeadTwinSearchId());
                if (CollectionUtils.isNotEmpty(headSearchPredicates) && basicSearch.getHeadSearch() == null)
                    basicSearch.setHeadSearch(new TwinSearch());
                addPredicates(headSearchPredicates, searchAlias.getParams(), basicSearch.getHeadSearch(), searchAlias.getNarrow());
            }
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

    public TwinSearchResult convertPageInTwinSearchResult(Page<TwinEntity> twinPage, int offset, int limit) {
        return (TwinSearchResult) new TwinSearchResult()
                .setTwinList(twinPage.getContent().stream().filter(t -> !twinService.isEntityReadDenied(t)).toList())
                .setOffset(offset)
                .setLimit(limit)
                .setTotal(twinPage.getTotalElements());
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

    public TwinSearchResult findTwins(SearchAlias searchAlias, int offset, int limit) throws ServiceException {
        return findTwins(searchAlias.getAlias(), searchAlias.getParams(), searchAlias.getNarrow(), offset, limit);
    }

    public TwinSearchResult findTwins(String searchAliasId, Map<String, String> namedParamsMap, BasicSearch searchNarrow, int offset, int limit) throws ServiceException {
        return findTwins(detectSearchesByAlias(searchAliasId), namedParamsMap, searchNarrow, offset, limit);
    }

    public TwinSearchResult findTwins(UUID searchId, Map<String, String> namedParamsMap, BasicSearch searchNarrow, int offset, int limit) throws ServiceException {
        return findTwins(entitySmartService.findById(searchId, searchRepository, EntitySmartService.FindMode.ifEmptyThrows), namedParamsMap, searchNarrow, offset, limit);
    }

    public TwinSearchResult findTwins(SearchEntity searchEntity, Map<String, String> namedParamsMap, BasicSearch searchNarrow, int offset, int limit) throws ServiceException {
        return findTwins(List.of(searchEntity), namedParamsMap, searchNarrow, offset, limit);
    }

    public TwinSearchResult findTwins(List<SearchEntity> searchEntities, Map<String, String> namedParamsMap, BasicSearch searchNarrow, int offset, int limit) throws ServiceException {
        SearchAlias searchAlias = new SearchAlias();
        searchAlias.setParams(namedParamsMap);
        searchAlias.setNarrow(searchNarrow);
        List<BasicSearch> basicSearches = getBasicSearchesByAlias(searchEntities, searchAlias);
        return findTwins(basicSearches, offset, limit);
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
        mainSearch.setTwinLinksMap(narrowMapOfSets(mainSearch.getTwinLinksMap(), narrowSearch.getTwinLinksMap()));
        mainSearch.setTwinNoLinksMap(narrowMapOfSets(mainSearch.getTwinNoLinksMap(), narrowSearch.getTwinNoLinksMap()));
    }
}
