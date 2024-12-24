package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepRepository;
import org.twins.core.domain.search.FactoryPipelineStepSearch;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.factory.FactoryPipelineStepSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryPipelineStepSearchService {
    private final TwinFactoryPipelineStepRepository twinFactoryPipelineStepRepository;

    public PaginationResult<TwinFactoryPipelineStepEntity> findFactoryPipelineSteps(FactoryPipelineStepSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryPipelineStepEntity> spec = createFactoryPipelineStepSearchSpecification(search);
        Page<TwinFactoryPipelineStepEntity> ret = twinFactoryPipelineStepRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryPipelineStepEntity> createFactoryPipelineStepSearchSpecification(FactoryPipelineStepSearch search) throws ServiceException {
        return Specification.where(
                checkOptional(search.getOptional())
                        .and(checkUuidIn(TwinFactoryPipelineStepEntity.Fields.id, search.getIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryPipelineStepEntity.Fields.id, search.getIdExcludeList(), true, false))
                        .and(checkUuidIn(TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId, search.getFactoryPipelineIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId, search.getFactoryPipelineIdExcludeList(), true, false))
                        .and(checkUuidIn(TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdExcludeList(), true, true))
                        .and(checkFieldLikeIn(TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId, search.getDescriptionLikeList(), false, false))
                        .and(checkFieldLikeIn(TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId, search.getDescriptionNotLikeList(), true, true))
                        .and(checkIntegerIn(search.getFillerFeaturerIdList(), false))
                        .and(checkIntegerIn(search.getFillerFeaturerIdExcludeList(), true))
        );
    }
}
