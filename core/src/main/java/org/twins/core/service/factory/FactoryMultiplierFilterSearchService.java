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
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterRepository;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.domain.search.FactoryMultiplierFilterSearch;


import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.factory.FactoryMultiplierFilterSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryMultiplierFilterSearchService {
    private final TwinFactoryMultiplierFilterRepository twinFactoryMultiplierFilterRepository;

    public PaginationResult<TwinFactoryMultiplierFilterEntity> findFactoryMultiplierFilters(FactoryMultiplierFilterSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryMultiplierFilterEntity> spec = createFactoryMultiplierFilterSearchSpecification(search);
        Page<TwinFactoryMultiplierFilterEntity> ret = twinFactoryMultiplierFilterRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryMultiplierFilterEntity> createFactoryMultiplierFilterSearchSpecification(FactoryMultiplierFilterSearch search) {
        return Specification.where(
                checkFieldLikeIn(TwinFactoryPipelineEntity.Fields.description, search.getDescriptionLikeList(), false, true)
                        .and(checkFieldLikeIn(TwinFactoryPipelineEntity.Fields.description, search.getDescriptionNotLikeList(), true, true))
                        .and(checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.id, search.getIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.id, search.getIdExcludeList(), true, false))
                        .and(checkFactoryIdIn(search.getFactoryIdList(), false))
                        .and(checkFactoryIdIn(search.getFactoryIdExcludeList(), true))
                        .and(checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.twinFactoryMultiplierId, search.getFactoryMultiplierIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.twinFactoryMultiplierId, search.getFactoryMultiplierIdExcludeList(), true, false))
                        .and(checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.inputTwinClassId, search.getInputTwinClassIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.inputTwinClassId, search.getInputTwinClassIdExcludeList(), true, false))
                        .and(checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryMultiplierFilterEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdExcludeList(), true, true))
                        .and(checkTernary(TwinFactoryMultiplierFilterEntity.Fields.active, search.getActive()))
        );
    }
}
