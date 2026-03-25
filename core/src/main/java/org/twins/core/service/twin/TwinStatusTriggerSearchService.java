package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinStatusTriggerEntity;
import org.twins.core.dao.twin.TwinStatusTriggerRepository;
import org.twins.core.domain.search.TwinStatusTriggerSearch;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class TwinStatusTriggerSearchService {
    private final TwinStatusTriggerRepository twinStatusTriggerRepository;

    @Transactional(readOnly = true)
    public PaginationResult<TwinStatusTriggerEntity> findStatusTriggers(TwinStatusTriggerSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinStatusTriggerEntity> spec = createTwinStatusTriggerSearchSpecification(search);
        Page<TwinStatusTriggerEntity> ret = twinStatusTriggerRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinStatusTriggerEntity> createTwinStatusTriggerSearchSpecification(TwinStatusTriggerSearch search) {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinStatusTriggerEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinStatusTriggerEntity.Fields.id),
                checkUuidIn(search.getTwinStatusIdList(), false, true, TwinStatusTriggerEntity.Fields.twinStatusId),
                checkUuidIn(search.getTwinStatusIdExcludeList(), true, true, TwinStatusTriggerEntity.Fields.twinStatusId),
                checkTernary(search.getIncomingElseOutgoing(), TwinStatusTriggerEntity.Fields.incomingElseOutgoing),
                checkUuidIn(search.getTwinTriggerIdList(), false, true, TwinStatusTriggerEntity.Fields.twinTriggerId),
                checkUuidIn(search.getTwinTriggerIdExcludeList(), true, true, TwinStatusTriggerEntity.Fields.twinTriggerId),
                checkTernary(search.getActive(), TwinStatusTriggerEntity.Fields.active),
                checkTernary(search.getAsync(), TwinStatusTriggerEntity.Fields.async)
        );
    }
}
