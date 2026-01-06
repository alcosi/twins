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
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepRepository;
import org.twins.core.domain.search.FactoryPipelineStepSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.specifications.factory.FactoryPipelineStepSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryPipelineStepSearchService {
    private final TwinFactoryPipelineStepRepository twinFactoryPipelineStepRepository;
    private final AuthService authService;

    public PaginationResult<TwinFactoryPipelineStepEntity> findFactoryPipelineSteps(FactoryPipelineStepSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryPipelineStepEntity> spec = createFactoryPipelineStepSearchSpecification(search);
        Page<TwinFactoryPipelineStepEntity> ret = twinFactoryPipelineStepRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryPipelineStepEntity> createFactoryPipelineStepSearchSpecification(FactoryPipelineStepSearch search) throws ServiceException {
        return Specification.allOf(
                checkDomainId(authService.getApiUser().getDomainId()),
                checkFactoryIdIn(search.getFactoryIdList(), false),
                checkFactoryIdIn(search.getFactoryIdExcludeList(), true),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryPipelineStepEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryPipelineStepEntity.Fields.id),
                checkUuidIn(search.getFactoryPipelineIdList(), false, false, TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId),
                checkUuidIn(search.getFactoryPipelineIdExcludeList(), true, false, TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId),
                checkUuidIn(search.getFactoryConditionSetIdList(), false, false, TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getFactoryConditionSetIdExcludeList(), true, true, TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionSetId),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, false, TwinFactoryPipelineStepEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryPipelineStepEntity.Fields.description),
                checkIntegerIn(search.getFillerFeaturerIdList(), false, TwinFactoryPipelineStepEntity.Fields.fillerFeaturerId),
                checkIntegerIn(search.getFillerFeaturerIdExcludeList(), true, TwinFactoryPipelineStepEntity.Fields.fillerFeaturerId),
                checkTernary(search.getConditionInvert(), TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionInvert),
                checkTernary(search.getOptional(), TwinFactoryPipelineStepEntity.Fields.optional),
                checkTernary(search.getActive(), TwinFactoryPipelineStepEntity.Fields.active)
        );
    }
}
