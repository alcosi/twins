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
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterRepository;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.domain.search.FactoryMultiplierFilterSearch;
import org.twins.core.service.auth.AuthService;

import java.util.Optional;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.factory.FactoryMultiplierFilterSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryMultiplierFilterSearchService {
    private final TwinFactoryMultiplierFilterRepository twinFactoryMultiplierFilterRepository;
    private final AuthService authService;
    @Transactional(readOnly = true)
    public TwinFactoryMultiplierFilterEntity findFactoryMultiplierFilterById(UUID id) throws ServiceException {
        Optional<TwinFactoryMultiplierFilterEntity> entity = twinFactoryMultiplierFilterRepository.findBy(
                Specification.allOf(
                        checkFieldUuid(id, TwinFactoryMultiplierFilterEntity.Fields.id)
                ), FluentQuery.FetchableFluentQuery::one
        );
        return entity.orElse(null);
    }
    public PaginationResult<TwinFactoryMultiplierFilterEntity> findFactoryMultiplierFilters(FactoryMultiplierFilterSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryMultiplierFilterEntity> spec = createFactoryMultiplierFilterSearchSpecification(search);
        Page<TwinFactoryMultiplierFilterEntity> ret = twinFactoryMultiplierFilterRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryMultiplierFilterEntity> createFactoryMultiplierFilterSearchSpecification(FactoryMultiplierFilterSearch search) {
        return Specification.allOf(
                checkFieldLikeIn(TwinFactoryPipelineEntity.Fields.description, search.getDescriptionLikeList(), false, true),
                checkFieldLikeIn(TwinFactoryPipelineEntity.Fields.description, search.getDescriptionNotLikeList(), true, true),
                checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.id, search.getIdList(), false, false),
                checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.id, search.getIdExcludeList(), true, false),
                checkFactoryIdIn(search.getFactoryIdList(), false),
                checkFactoryIdIn(search.getFactoryIdExcludeList(), true),
                checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.twinFactoryMultiplierId, search.getFactoryMultiplierIdList(), false, false),
                checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.twinFactoryMultiplierId, search.getFactoryMultiplierIdExcludeList(), true, false),
                checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.inputTwinClassId, search.getInputTwinClassIdList(), false, false),
                checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.inputTwinClassId, search.getInputTwinClassIdExcludeList(), true, false),
                checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdList(), false, false),
                checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdExcludeList(), true, true),
                checkTernary(TwinFactoryMultiplierFilterEntity.Fields.active, search.getActive()));
    }
}
