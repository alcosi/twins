package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepRepository;
import org.twins.core.domain.search.FactoryPipelineStepSearch;
import org.twins.core.service.auth.AuthService;

import java.util.Optional;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.factory.FactoryPipelineStepSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryPipelineStepSearchService {
    private final TwinFactoryPipelineStepRepository twinFactoryPipelineStepRepository;
    private final AuthService authService;
    @Transactional(readOnly = true)
    public TwinFactoryPipelineStepEntity findFactoryPipelineStepsById(UUID id) throws ServiceException {
        Optional<TwinFactoryPipelineStepEntity> entity = twinFactoryPipelineStepRepository.findBy(
                Specification.allOf(
                        checkFieldUuid(authService.getApiUser().getDomainId(), TwinFactoryPipelineStepEntity.Fields.twinFactoryPipeline,TwinFactoryPipelineEntity.Fields.twinFactoryId, TwinFactoryEntity.Fields.domainId),
                        checkFieldUuid(id, TwinFactoryPipelineStepEntity.Fields.id)
                ), FluentQuery.FetchableFluentQuery::one
        );
        return entity.orElse(null);
    }
    public PaginationResult<TwinFactoryPipelineStepEntity> findFactoryPipelineSteps(FactoryPipelineStepSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryPipelineStepEntity> spec = createFactoryPipelineStepSearchSpecification(search);
        Page<TwinFactoryPipelineStepEntity> ret = twinFactoryPipelineStepRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryPipelineStepEntity> createFactoryPipelineStepSearchSpecification(FactoryPipelineStepSearch search) throws ServiceException {
        return Specification.where(
                checkTernary(TwinFactoryPipelineStepEntity.Fields.optional, search.getOptional())
                        .and(checkUuidIn(TwinFactoryPipelineStepEntity.Fields.id, search.getIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryPipelineStepEntity.Fields.id, search.getIdExcludeList(), true, false))
                        .and(checkUuidIn(TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId, search.getFactoryPipelineIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId, search.getFactoryPipelineIdExcludeList(), true, false))
                        .and(checkUuidIn(TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdExcludeList(), true, true))
                        .and(checkFieldLikeIn(TwinFactoryPipelineStepEntity.Fields.description, search.getDescriptionLikeList(), false, false))
                        .and(checkFieldLikeIn(TwinFactoryPipelineStepEntity.Fields.description, search.getDescriptionNotLikeList(), true, true))
                        .and(checkIntegerIn(TwinFactoryPipelineStepEntity.Fields.fillerFeaturerId, search.getFillerFeaturerIdList(), false))
                        .and(checkIntegerIn(TwinFactoryPipelineStepEntity.Fields.fillerFeaturerId, search.getFillerFeaturerIdExcludeList(), true))
        );
    }
}
