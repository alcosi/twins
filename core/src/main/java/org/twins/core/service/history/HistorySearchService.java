package org.twins.core.service.history;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
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

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.history.HistorySpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class HistorySearchService {
    private final HistoryRepository historyRepository;

    public PaginationResult<HistoryEntity> findHistory(HistorySearch search, SimplePagination pagination) throws ServiceException {
        Specification<HistoryEntity> spec = createHisotrySearchSpecification(search);
        Page<HistoryEntity> ret = historyRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<HistoryEntity> createHisotrySearchSpecification(HistorySearch search) {
        return checkByTwinIdIncludeFirstLevelChildren(search.getTwinIdList(), search.isIncludeDirectChildren(), false)
                .and(checkByTwinIdIncludeFirstLevelChildren(search.getTwinIdExcludeList(), false, true))
                .and(checkUuidIn(search.getIdList(), false, false, HistoryEntity.Fields.id))
                .and(checkUuidIn(search.getIdExcludeList(), true, false, HistoryEntity.Fields.id))
                .and(checkUuidIn(search.getTwinIdList(), false, false, HistoryEntity.Fields.twinId))
                .and(checkUuidIn(search.getTwinIdExcludeList(), true, false, HistoryEntity.Fields.twinId))
                .and(checkUuidIn(search.getActorUseridList(), false, false, HistoryEntity.Fields.actorUserId))
                .and(checkUuidIn(search.getActorUserIdExcludeList(), true, false, HistoryEntity.Fields.actorUserId))
                .and(checkUuidIn(search.getTwinClassFieldIdList(), false, false, HistoryEntity.Fields.twinClassFieldId))
                .and(checkUuidIn(search.getTwinClassFieldIdExcludeList(), true, false, HistoryEntity.Fields.twinClassFieldId))
                .and(checkType(search.getTypeList(), false))
                .and(checkType(search.getTypeExcludeList(), true))
                .and(createdAtBetween(search.getCreatedAt()));
    }
}
