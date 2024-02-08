package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.PaginationUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.BasicSearch;
import org.twins.core.service.auth.AuthService;

import java.util.*;

import static org.cambium.common.util.PaginationUtils.sort;
import static org.springframework.data.jpa.domain.Specification.not;
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
        boolean links = MapUtils.isNotEmpty(basicSearch.getTwinLinksMap()) || MapUtils.isNotEmpty(null);
        ApiUser apiUser = authService.getApiUser();
        return where(links ? checkJoinLinksSingleSpecification(basicSearch, apiUser) :
                checkUuidIn(TwinEntity.Fields.id, basicSearch.getTwinIdList()))
                .and(not(checkUuidIn(TwinEntity.Fields.id, basicSearch.getTwinIdExcludeList())))
                .and(checkFieldLikeIn(TwinEntity.Fields.name, basicSearch.getTwinNameLikeList(), true))
                .and(checkClass(basicSearch.getTwinClassIdList(), apiUser))
                //todo create filter by basicSearch.getExtendsTwinClassIdList()
                .and(checkUuidIn(TwinEntity.Fields.assignerUserId, basicSearch.getAssignerUserIdList()))
                .and(checkUuidIn(TwinEntity.Fields.createdByUserId, basicSearch.getCreatedByUserIdList()))
                .and(checkUuidIn(TwinEntity.Fields.twinStatusId, basicSearch.getStatusIdList()))
                .and(checkUuidIn(TwinEntity.Fields.headTwinId, basicSearch.getHeaderTwinIdList()));
    }

    public List<TwinEntity> findTwins(BasicSearch basicSearch) throws ServiceException {
        List<TwinEntity> ret = twinRepository.findAll(createTwinEntitySearchSpecification(basicSearch), sort(false, TwinEntity.Fields.createdAt));
        return ret.stream().filter(t -> !twinService.isEntityReadDenied(t)).toList();
    }

    public TwinSearchResult findTwins(BasicSearch basicSearch, int offset, int size) throws ServiceException {
        TwinSearchResult twinSearchResult = new TwinSearchResult();
        Specification<TwinEntity> spec = createTwinEntitySearchSpecification(basicSearch);
        Page<TwinEntity> ret = twinRepository.findAll(where(spec), PaginationUtils.pagination(offset / size, size, sort(false, TwinEntity.Fields.createdAt)));
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
