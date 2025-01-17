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
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineRepository;
import org.twins.core.domain.search.FactoryPipelineSearch;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldLikeIn;
import static org.twins.core.dao.specifications.factory.FactoryConditionSetSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.factory.FactoryPipelineSpecification.checkTernary;


@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryPipelineSearchService {
    private final TwinFactoryPipelineRepository twinFactoryPipelineRepository;

    public PaginationResult<TwinFactoryPipelineEntity> findFactoryConditionSets(FactoryPipelineSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryPipelineEntity> spec = createFactoryPipelineSearchSpecification(search);
        Page<TwinFactoryPipelineEntity> ret = twinFactoryPipelineRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryPipelineEntity> createFactoryPipelineSearchSpecification(FactoryPipelineSearch search) {
        return Specification.allOf(
                checkFieldLikeIn(TwinFactoryPipelineEntity.Fields.description, search.getDescriptionLikeList(), false, true),
                checkFieldLikeIn(TwinFactoryPipelineEntity.Fields.description, search.getDescriptionNotLikeList(), true, true),
                checkUuidIn(TwinFactoryPipelineEntity.Fields.id, search.getIdList(), false, false),
                checkUuidIn(TwinFactoryPipelineEntity.Fields.id, search.getIdExcludeList(), true, false),
                checkUuidIn(TwinFactoryPipelineEntity.Fields.twinFactoryId, search.getFactoryIdList(), false, false),
                checkUuidIn(TwinFactoryPipelineEntity.Fields.twinFactoryId, search.getFactoryIdExcludeList(), true, false),
                checkUuidIn(TwinFactoryPipelineEntity.Fields.inputTwinClassId, search.getInputTwinClassIdList(), false, false),
                checkUuidIn(TwinFactoryPipelineEntity.Fields.inputTwinClassId, search.getInputTwinClassIdExcludeList(), true, false),
                checkUuidIn(TwinFactoryPipelineEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdList(), false, false),
                checkUuidIn(TwinFactoryPipelineEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdExcludeList(), true, true),
                checkUuidIn(TwinFactoryPipelineEntity.Fields.outputTwinStatusId, search.getOutputTwinStatusIdList(), false, false),
                checkUuidIn(TwinFactoryPipelineEntity.Fields.outputTwinStatusId, search.getOutputTwinStatusIdExcludeList(), true, true),
                checkUuidIn(TwinFactoryPipelineEntity.Fields.nextTwinFactoryId, search.getNextFactoryIdList(), false, false),
                checkUuidIn(TwinFactoryPipelineEntity.Fields.nextTwinFactoryId, search.getNextFactoryIdExcludeList(), true, true),
                checkTernary(TwinFactoryPipelineEntity.Fields.active, search.getActive()),
                checkTernary(TwinFactoryPipelineEntity.Fields.nextTwinFactoryLimitScope, search.getNextFactoryLimitScope()));
    }
}
