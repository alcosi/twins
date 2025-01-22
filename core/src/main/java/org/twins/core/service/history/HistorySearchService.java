package org.twins.core.service.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.history.HistoryRepository;
import org.twins.core.domain.search.HistorySearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.history.HistorySpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class HistorySearchService {

    private final HistoryRepository historyRepository;
    private final AuthService authService;

    public PaginationResult<HistoryEntity> findHistory(HistorySearch search, SimplePagination pagination) throws ServiceException {
        Specification<HistoryEntity> spec = createHisotrySearchSpecification(search);
        Page<HistoryEntity> ret = historyRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<HistoryEntity> createHisotrySearchSpecification(HistorySearch search) {
        return Specification.where(
                checkByTwinIdIncludeFirstLevelChildren(search.getTwinIdList(), search.isIncludeDirectChildren(), false)
                        .and(checkByTwinIdIncludeFirstLevelChildren(search.getTwinIdExcludeList(), false, true))
                        .and(checkUuidIn(HistoryEntity.Fields.id, search.getIdList(), false, false))
                        .and(checkUuidIn(HistoryEntity.Fields.id, search.getIdExcludeList(), true, false))
                        .and(checkUuidIn(HistoryEntity.Fields.actorUserId, search.getActorUseridList(), false, false))
                        .and(checkUuidIn(HistoryEntity.Fields.actorUserId, search.getActorUserIdExcludeList(), true, false))
                        .and(checkType(search.getTypeList(), false))
                        .and(checkType(search.getTypeExcludeList(), true))
                        .and(createdAtBetween(search.getCreatedAt()))
        );
    }

}
