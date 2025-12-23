package org.twins.core.service.factory;

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
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineRepository;
import org.twins.core.domain.search.FactoryPipelineSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.specifications.factory.FactoryPipelineSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryPipelineSearchService {
    private final TwinFactoryPipelineRepository twinFactoryPipelineRepository;
    private final AuthService authService;

    public PaginationResult<TwinFactoryPipelineEntity> findFactoryPipelines(FactoryPipelineSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryPipelineEntity> spec = createFactoryPipelineSearchSpecification(search);
        Page<TwinFactoryPipelineEntity> ret = twinFactoryPipelineRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryPipelineEntity> createFactoryPipelineSearchSpecification(FactoryPipelineSearch search) throws ServiceException {
        return Specification.allOf(
                checkDomainId(authService.getApiUser().getDomainId()),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinFactoryPipelineEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryPipelineEntity.Fields.description),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryPipelineEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryPipelineEntity.Fields.id),
                checkUuidIn(search.getFactoryIdList(), false, false, TwinFactoryPipelineEntity.Fields.twinFactoryId),
                checkUuidIn(search.getFactoryIdExcludeList(), true, false, TwinFactoryPipelineEntity.Fields.twinFactoryId),
                checkUuidIn(search.getInputTwinClassIdList(), false, false, TwinFactoryPipelineEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getInputTwinClassIdExcludeList(), true, false, TwinFactoryPipelineEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getFactoryConditionSetIdList(), false, false, TwinFactoryPipelineEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getFactoryConditionSetIdExcludeList(), true, true, TwinFactoryPipelineEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getOutputTwinStatusIdList(), false, false, TwinFactoryPipelineEntity.Fields.outputTwinStatusId),
                checkUuidIn(search.getOutputTwinStatusIdExcludeList(), true, true, TwinFactoryPipelineEntity.Fields.outputTwinStatusId),
                checkUuidIn(search.getNextFactoryIdList(), false, false, TwinFactoryPipelineEntity.Fields.nextTwinFactoryId),
                checkUuidIn(search.getNextFactoryIdExcludeList(), true, true, TwinFactoryPipelineEntity.Fields.nextTwinFactoryId),
                checkTernary(search.getActive(), TwinFactoryPipelineEntity.Fields.active),
                checkTernary(search.getNextFactoryLimitScope(), TwinFactoryPipelineEntity.Fields.nextTwinFactoryLimitScope));
    }
}
