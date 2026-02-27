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
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerRepository;
import org.twins.core.domain.search.TwinStatusTransitionTriggerSearch;

import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.specifications.twinstatus.TwinStatusTransitionTriggerSpecification.*;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class TwinStatusTransitionTriggerSearchService {
    private final TwinStatusTransitionTriggerRepository twinStatusTransitionTriggerRepository;

    @Transactional(readOnly = true)
    public PaginationResult<TwinStatusTransitionTriggerEntity> findStatusTransitionTriggers(TwinStatusTransitionTriggerSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinStatusTransitionTriggerEntity> spec = createTwinStatusTransitionTriggerSearchSpecification(search);
        Page<TwinStatusTransitionTriggerEntity> ret = twinStatusTransitionTriggerRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinStatusTransitionTriggerEntity> createTwinStatusTransitionTriggerSearchSpecification(TwinStatusTransitionTriggerSearch search) {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinStatusTransitionTriggerEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinStatusTransitionTriggerEntity.Fields.id),
                checkUuidIn(search.getTwinStatusIdList(), false, true, TwinStatusTransitionTriggerEntity.Fields.twinStatusId),
                checkUuidIn(search.getTwinStatusIdExcludeList(), true, true, TwinStatusTransitionTriggerEntity.Fields.twinStatusId),
                checkTransitionTypeIn(search.getTypeList(), false),
                checkTransitionTypeIn(search.getTypeExcludeList(), true),
                checkUuidIn(search.getTwinTriggerIdList(), false, true, TwinStatusTransitionTriggerEntity.Fields.twinTriggerId),
                checkUuidIn(search.getTwinTriggerIdExcludeList(), true, true, TwinStatusTransitionTriggerEntity.Fields.twinTriggerId),
                checkTernary(search.getActive(), TwinStatusTransitionTriggerEntity.Fields.active),
                checkTernary(search.getAsync(), TwinStatusTransitionTriggerEntity.Fields.async)
        );
    }
}
