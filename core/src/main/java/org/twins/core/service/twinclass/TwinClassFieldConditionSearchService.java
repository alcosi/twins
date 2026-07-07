package org.twins.core.service.twinclass;

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
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldConditionRepository;
import org.twins.core.domain.search.TwinClassFieldConditionSearch;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFieldConditionSearchService {

    private final TwinClassFieldConditionRepository twinClassFieldConditionRepository;

    public PaginationResult<TwinClassFieldConditionEntity> findTwinClassFieldConditions(TwinClassFieldConditionSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinClassFieldConditionEntity> spec = createTwinClassFieldConditionSearchSpecification(search);
        Page<TwinClassFieldConditionEntity> ret = twinClassFieldConditionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinClassFieldConditionEntity> createTwinClassFieldConditionSearchSpecification(TwinClassFieldConditionSearch search) throws ServiceException {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinClassFieldConditionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinClassFieldConditionEntity.Fields.id),
                checkUuidIn(search.getTwinClassFieldRuleIdList(), false, false, TwinClassFieldConditionEntity.Fields.twinClassFieldRuleId),
                checkUuidIn(search.getTwinClassFieldRuleIdExcludeList(), true, false, TwinClassFieldConditionEntity.Fields.twinClassFieldRuleId),
                checkUuidIn(search.getBaseTwinClassFieldIdList(), false, false, TwinClassFieldConditionEntity.Fields.baseTwinClassFieldId),
                checkUuidIn(search.getBaseTwinClassFieldIdExcludeList(), true, false, TwinClassFieldConditionEntity.Fields.baseTwinClassFieldId),
                checkUuidIn(search.getParentTwinClassFieldConditionIdList(), false, false, TwinClassFieldConditionEntity.Fields.parentTwinClassFieldConditionId),
                checkUuidIn(search.getParentTwinClassFieldConditionIdExcludeList(), true, false, TwinClassFieldConditionEntity.Fields.parentTwinClassFieldConditionId),
                checkFieldIn(search.getLogicOperatorIdList(), false, TwinClassFieldConditionEntity.Fields.logicOperatorId),
                checkFieldIn(search.getLogicOperatorIdExcludeList(), true, TwinClassFieldConditionEntity.Fields.logicOperatorId),
                checkIntegerIn(search.getConditionEvaluatorFeaturerIdList(), false, TwinClassFieldConditionEntity.Fields.conditionEvaluatorFeaturerId),
                checkIntegerIn(search.getConditionEvaluatorFeaturerIdExcludeList(), true, TwinClassFieldConditionEntity.Fields.conditionEvaluatorFeaturerId)
        );
    }
}
