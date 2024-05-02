package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.PaginationUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserGroupService;

import java.util.*;

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
    @Lazy
    final AuthService authService;

    private Specification<TwinEntity> createTwinEntityBasicSearchSpecification(TwinSearch twinSearch) throws ServiceException {

        return where(
                checkTwinLinks(twinSearch.getTwinLinksMap(), twinSearch.getTwinNoLinksMap())
                        .and(checkUuidIn(TwinEntity.Fields.id, twinSearch.getTwinIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.id, twinSearch.getTwinIdExcludeList(), true))
                        .and(checkFieldLikeIn(TwinEntity.Fields.name, twinSearch.getTwinNameLikeList(), true))
                        .and(checkUuidIn(TwinEntity.Fields.assignerUserId, twinSearch.getAssignerUserIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.createdByUserId, twinSearch.getCreatedByUserIdList(), false))
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

    public TwinSearchResult findTwins(BasicSearch basicSearch, int offset, int size) throws ServiceException {
        TwinSearchResult twinSearchResult = new TwinSearchResult();
        Specification<TwinEntity> spec = createTwinEntitySearchSpecification(basicSearch);
        Page<TwinEntity> ret = twinRepository.findAll(where(spec), PaginationUtils.paginationOffset(offset, size, sort(false, TwinEntity.Fields.createdAt)));
        return (TwinSearchResult) twinSearchResult
                .setTwinList(ret.getContent().stream().filter(t -> !twinService.isEntityReadDenied(t)).toList())
                .setOffset(offset)
                .setLimit(size)
                .setTotal(ret.getTotalElements());
    }

    public TwinSearchResult findTwins(List<BasicSearch> basicSearches, int offset, int size) throws ServiceException {
        TwinSearchResult twinSearchResult = new TwinSearchResult();
        Specification<TwinEntity> spec = where(null);
        for (BasicSearch basicSearch : basicSearches)
            spec = spec.or(createTwinEntitySearchSpecification(basicSearch));
        Page<TwinEntity> ret = twinRepository.findAll(spec, PaginationUtils.paginationOffset(offset, size, sort(false, TwinEntity.Fields.createdAt)));
        return (TwinSearchResult) twinSearchResult
                .setTwinList(ret.getContent().stream().filter(t -> !twinService.isEntityReadDenied(t)).toList())
                .setOffset(offset)
                .setLimit(size)
                .setTotal(ret.getTotalElements());
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


}
