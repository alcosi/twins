package org.twins.core.service.trigger;

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
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.trigger.TwinTriggerRepository;
import org.twins.core.domain.search.TwinTriggerSearch;

import static org.twins.core.dao.specifications.CommonSpecification.checkIntegerIn;
import static org.twins.core.dao.specifications.CommonSpecification.checkTernary;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.CommonSpecification.checkFieldLikeIn;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
@Service
public class TwinTriggerSearchService {
    private final TwinTriggerRepository twinTriggerRepository;

    public PaginationResult<TwinTriggerEntity> findTwinTriggers(TwinTriggerSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinTriggerEntity> spec = createTwinTriggerSearchSpecification(search);
        Page<TwinTriggerEntity> ret = twinTriggerRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinTriggerEntity> createTwinTriggerSearchSpecification(TwinTriggerSearch search) throws ServiceException {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinTriggerEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinTriggerEntity.Fields.id),
                checkIntegerIn(search.getTriggerFeaturerIdList(), false, TwinTriggerEntity.Fields.twinTriggerFeaturerId),
                checkIntegerIn(search.getTriggerFeaturerIdExcludeList(), true, TwinTriggerEntity.Fields.twinTriggerFeaturerId),
                checkTernary(search.getActive(), TwinTriggerEntity.Fields.active),
                checkFieldLikeIn(search.getNameLikeList(), false, true, TwinTriggerEntity.Fields.name)
        );
    }
}
