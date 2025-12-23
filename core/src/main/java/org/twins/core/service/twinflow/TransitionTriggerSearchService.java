package org.twins.core.service.twinflow;

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
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerRepository;
import org.twins.core.domain.search.TransitionTriggerSearch;

import static org.twins.core.dao.specifications.CommonSpecification.checkTernary;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.twinflow.TransitionTriggerSpecification.checkIntegerIn;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TransitionTriggerSearchService {
    private final TwinflowTransitionTriggerRepository twinflowTransitionTriggerRepository;

    public PaginationResult<TwinflowTransitionTriggerEntity> findTransitionTriggers(TransitionTriggerSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinflowTransitionTriggerEntity> spec = createTransitionTriggerSearchSpecification(search);
        Page<TwinflowTransitionTriggerEntity> ret = twinflowTransitionTriggerRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinflowTransitionTriggerEntity> createTransitionTriggerSearchSpecification(TransitionTriggerSearch search) {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinflowTransitionTriggerEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinflowTransitionTriggerEntity.Fields.id),
                checkUuidIn(search.getTwinflowTransitionIdList(), false, false, TwinflowTransitionTriggerEntity.Fields.twinflowTransitionId),
                checkUuidIn(search.getTwinflowTransitionIdExcludeList(), true, true, TwinflowTransitionTriggerEntity.Fields.twinflowTransitionId),
                checkIntegerIn(search.getTransitionTriggerFeaturerIdList(), false, TwinflowTransitionTriggerEntity.Fields.transitionTriggerFeaturerId),
                checkIntegerIn(search.getTransitionTriggerFeaturerIdExcludeList(), true, TwinflowTransitionTriggerEntity.Fields.transitionTriggerFeaturerId),
                checkTernary(search.getActive(), TwinflowTransitionTriggerEntity.Fields.isActive)
        );
    }
}
