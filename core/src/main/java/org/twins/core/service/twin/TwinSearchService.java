package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.PaginationUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.search.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.search.function.SearchFunction;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.user.UserGroupService;

import java.util.*;
import java.util.stream.Collectors;

import static org.cambium.common.util.PaginationUtils.sort;
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
    final PermissionService permissionService;
    @Lazy
    final AuthService authService;

    private Specification<TwinEntity> createTwinEntityBasicSearchSpecification(TwinSearch twinSearch) throws ServiceException {

        return where(
                checkTwinLinks(twinSearch.getTwinLinksMap(), twinSearch.getTwinNoLinksMap())
                        .and(checkUuidIn(TwinEntity.Fields.id, twinSearch.getTwinIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.id, twinSearch.getTwinIdExcludeList(), true))
                        .and(checkFieldLikeIn(TwinEntity.Fields.name, twinSearch.getTwinNameLikeList(), true))
                        .and(checkUuidIn(TwinEntity.Fields.assignerUserId, twinSearch.getAssignerUserIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.assignerUserId, twinSearch.getAssignerUserIdExcludeList(), true))
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
        Specification<TwinEntity> spec = createTwinEntitySearchSpecification(basicSearch);
        Page<TwinEntity> ret = twinRepository.findAll(where(spec), PaginationUtils.paginationOffset(offset, limit, sort(false, TwinEntity.Fields.createdAt)));
        return convertPageInTwinSearchResult(ret, offset, limit);
    }

    public TwinSearchResult findTwins(List<BasicSearch> basicSearches, int offset, int limit) throws ServiceException {
        Specification<TwinEntity> spec = where(null);
        for (BasicSearch basicSearch : basicSearches)
            spec = spec.or(createTwinEntitySearchSpecification(basicSearch));
        Page<TwinEntity> ret = twinRepository.findAll(spec, PaginationUtils.paginationOffset(offset, limit, sort(false, TwinEntity.Fields.createdAt)));
        return convertPageInTwinSearchResult(ret, offset, limit);
    }

    public Long count(BasicSearch basicSearch) throws ServiceException {
        return twinRepository.count(createTwinEntitySearchSpecification(basicSearch));
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

    public SearchEntity detectSearchByAlias(String searchAliasId) throws ServiceException {
        List<SearchEntity> searchEntityList = searchRepository.findBySearchAliasId(searchAliasId);
        if (searchEntityList == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_ALIAS_UNKNOWN);
        ApiUser apiUser = authService.getApiUser();
        permissionService.loadUserPermissions(apiUser);
        SearchEntity searchEntity = null;
        for (SearchEntity searchByAliasEntity : searchEntityList) { //many searches can be linked to one alias
            if (searchByAliasEntity.getPermissionId() == null || apiUser.getPermissions().contains(searchByAliasEntity.getPermissionId())) {
                if (searchEntity == null)
                    searchEntity = searchByAliasEntity;
                else
                    throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_NOT_UNIQ);
            }
        }
        if (searchEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_ALIAS_UNKNOWN);
        return searchEntity;
    }

    public List<TwinEntity> findTwins(String searchAliasId, Map<String, UUID> namedParamsMap) throws ServiceException {
        return findTwins(detectSearchByAlias(searchAliasId), namedParamsMap);
    }

    public List<TwinEntity> findTwins(SearchEntity searchEntity, Map<String, UUID> namedParamsMap, TwinSearch searchNarrow) throws ServiceException {
        if (CollectionUtils.isNotEmpty(searchEntity.getSearchParamList())) {
            if (searchEntity.getSearchParamList().size() != MapUtils.size(namedParamsMap))
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_PARAMS_COUNT_INCORRECT, searchEntity.logShort() + " expected " + searchEntity.getSearchParamList().size())
        }
        BasicSearch basicSearch = new BasicSearch();
        if (CollectionUtils.isNotEmpty(searchEntity.getSearchByTwinList())) {
            for (SearchByTwinEntity searchByTwinEntity : searchEntity.getSearchByTwinList())
                basicSearch.addTwinId(searchByTwinEntity.getTwinId(), searchByTwinEntity.isExclude());
        }
        if (CollectionUtils.isNotEmpty(searchEntity.getSearchByTwinStatusList())) {
            for (SearchByTwinStatusEntity searchByTwinStatusEntity : searchEntity.getSearchByTwinStatusList())
                basicSearch.addStatusId(searchByTwinStatusEntity.getTwinStatusId(), searchByTwinStatusEntity.isExclude());
        }
        if (CollectionUtils.isNotEmpty(searchEntity.getSearchByTwinClassList())) {
            for (SearchByTwinClassEntity searchByTwinClassEntity : searchEntity.getSearchByTwinClassList())
                basicSearch.addTwinClassId(searchByTwinClassEntity.getTwinClassId(), searchByTwinClassEntity.isExclude());
        }
        if (CollectionUtils.isNotEmpty(searchEntity.getSearchByUserList())) {
            for (SearchByUserEntity searchByUserEntity : searchEntity.getSearchByUserList()) {
                SearchFunction searchFunction = null;
                if (searchByUserEntity.getSearchParam() != null) {
                    searchFunction = featurerService.getFeaturer(searchByUserEntity.getSearchParam().getSearchFunctionFeaturer(), SearchFunction.class);
                    if (!searchFunction.validForField(searchByUserEntity.getSearchField()))
                        log.warn(searchByUserEntity.logShort() + " incorrect config. Search function [" + searchByUserEntity.getSearchParam().getSearchFunctionFeaturer().getName() + "] can not be applied for field[" + searchByUserEntity.getSearchField() + "].");
                }
                switch (searchByUserEntity.getSearchField()) {
                    case assigneeUserId:
                        if (searchByUserEntity.getUserId() != null)
                            basicSearch.addAssignerUserId(searchByUserEntity.getUserId(), false);
                        if (searchFunction != null)
                            basicSearch.addAssignerUserId(searchFunction.getId(searchByUserEntity.getSearchParam()), false);
                        break;
                    case createdByUserId:
                        if (searchByUserEntity.getUserId() != null)
                            basicSearch.addCreatedByUserId(searchByUserEntity.getUserId(), false);
                        if (searchFunction != null) {
                            basicSearch.addCreatedByUserId(searchFunction.getId(searchByUserEntity.getSearchParam()), false);
                        }
                        break;
                    default:
                        log.warn(searchByUserEntity.logShort() + " incorrect config. Search field [" + searchByUserEntity.getSearchField() + "] can not be applied for user.");
                }
            }
            basicSearch.setAssignerUserIdList(searchEntity.getSearchByTwinClassList().stream().map(SearchByTwinClassEntity::getTwinClassId).collect(Collectors.toSet()));
        }
        if (CollectionUtils.isNotEmpty(searchEntity.getSearchByLinkList())) {
            for (SearchByLinkEntity searchByLinkEntity : searchEntity.getSearchByLinkList()) {
                basicSearch.addLinkDstTwinsId(searchByLinkEntity.getLinkId(), searchByLinkEntity.getDstTwinId());
            }
        }
        return findTwins(basicSearch);
    }
}
