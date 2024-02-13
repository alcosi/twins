package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
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
import org.twins.core.domain.BasicSearch;
import org.twins.core.service.auth.AuthService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Lazy
    final AuthService authService;

    private Specification<TwinEntity> createTwinEntitySearchSpecification(BasicSearch basicSearch) throws ServiceException {
        return where(
                checkTwinLinks(basicSearch.getTwinLinksMap(), basicSearch.getTwinNoLinksMap())
                        .and(checkUuidIn(TwinEntity.Fields.id, basicSearch.getTwinIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.id, basicSearch.getTwinIdExcludeList(), true))
                        .and(checkFieldLikeIn(TwinEntity.Fields.name, basicSearch.getTwinNameLikeList(), true))
                        .and(checkClass(basicSearch.getTwinClassIdList(), authService.getApiUser()))
                        //todo create filter by basicSearch.getExtendsTwinClassIdList()
                        .and(checkUuidIn(TwinEntity.Fields.assignerUserId, basicSearch.getAssignerUserIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.createdByUserId, basicSearch.getCreatedByUserIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.twinStatusId, basicSearch.getStatusIdList(), false))
                        .and(checkUuidIn(TwinEntity.Fields.headTwinId, basicSearch.getHeaderTwinIdList(), false))
        );
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
        if(offset % size > 0) throw new ServiceException(PAGINATION_ERROR);
        TwinSearchResult twinSearchResult = new TwinSearchResult();
        Specification<TwinEntity> spec = where(null);
        for(BasicSearch basicSearch : basicSearches)
            spec = spec.or(createTwinEntitySearchSpecification(basicSearch));
        Page<TwinEntity> ret = twinRepository.findAll(spec, PaginationUtils.pagination(offset / size, size, sort(false, TwinEntity.Fields.createdAt)));
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


}
