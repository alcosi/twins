package org.twins.core.service.factory;

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
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dao.trigger.TwinFactoryTriggerRepository;
import org.twins.core.domain.search.TwinFactoryTriggerSearch;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class TwinFactoryTriggerSearchService {
    private final TwinFactoryTriggerRepository twinFactoryTriggerRepository;

    @Transactional(readOnly = true)
    public PaginationResult<TwinFactoryTriggerEntity> findFactoryTriggers(TwinFactoryTriggerSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryTriggerEntity> spec = createTwinFactoryTriggerSearchSpecification(search);
        Page<TwinFactoryTriggerEntity> ret = twinFactoryTriggerRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryTriggerEntity> createTwinFactoryTriggerSearchSpecification(TwinFactoryTriggerSearch search) {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinFactoryTriggerEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryTriggerEntity.Fields.id),
                checkUuidIn(search.getTwinFactoryIdList(), false, true, TwinFactoryTriggerEntity.Fields.twinFactoryId),
                checkUuidIn(search.getTwinFactoryIdExcludeList(), true, true, TwinFactoryTriggerEntity.Fields.twinFactoryId),
                checkUuidIn(search.getInputTwinClassIdList(), false, true, TwinFactoryTriggerEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getInputTwinClassIdExcludeList(), true, true, TwinFactoryTriggerEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getTwinTriggerIdList(), false, true, TwinFactoryTriggerEntity.Fields.twinTriggerId),
                checkUuidIn(search.getTwinTriggerIdExcludeList(), true, true, TwinFactoryTriggerEntity.Fields.twinTriggerId),
                checkTernary(search.getActive(), TwinFactoryTriggerEntity.Fields.active),
                checkTernary(search.getAsync(), TwinFactoryTriggerEntity.Fields.async)
        );
    }
}
