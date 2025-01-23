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

    private Specification<TwinFactoryPipelineStepEntity> createFactoryPipelineStepSearchSpecification(FactoryPipelineStepSearch search) {
        return Specification.where(
                checkTernary(TwinFactoryPipelineStepEntity.Fields.optional, search.getOptional())
                        .and(checkUuidIn(search.getIdList(), false, false, TwinFactoryPipelineStepEntity.Fields.id))
                        .and(checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryPipelineStepEntity.Fields.id))
                        .and(checkUuidIn(search.getFactoryPipelineIdList(), false, false, TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId))
                        .and(checkUuidIn(search.getFactoryPipelineIdExcludeList(), true, false, TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId))
                        .and(checkUuidIn(search.getFactoryConditionSetIdList(), false, false, TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionSetId))
                        .and(checkUuidIn(search.getFactoryConditionSetIdExcludeList(), true, true, TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionSetId))
                        .and(checkFieldLikeIn(search.getDescriptionLikeList(), false, false, TwinFactoryPipelineStepEntity.Fields.description))
                        .and(checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryPipelineStepEntity.Fields.description))
                        .and(checkIntegerIn(TwinFactoryPipelineStepEntity.Fields.fillerFeaturerId, search.getFillerFeaturerIdList(), false))
                        .and(checkIntegerIn(TwinFactoryPipelineStepEntity.Fields.fillerFeaturerId, search.getFillerFeaturerIdExcludeList(), true))
                        .and(checkFactoryIdIn(search.getFactoryIdList(), false))
                        .and(checkFactoryIdIn(search.getFactoryIdExcludeList(), true))
        );
    }
}
