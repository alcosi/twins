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
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierRepository;
import org.twins.core.domain.search.FactoryMultiplierSearch;

import static org.twins.core.dao.specifications.factory.FactoryMultiplierSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryMultiplierSearchService {
    private final TwinFactoryMultiplierRepository twinFactoryMultiplierRepository;

    public PaginationResult<TwinFactoryMultiplierEntity> findFactoryMultipliers(FactoryMultiplierSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryMultiplierEntity> spec = createFactoryMultiplierSearchSpecification(search);
        Page<TwinFactoryMultiplierEntity> ret = twinFactoryMultiplierRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryMultiplierEntity> createFactoryMultiplierSearchSpecification(FactoryMultiplierSearch search) throws ServiceException {
        return Specification.where(
                checkTernary(TwinFactoryMultiplierEntity.Fields.active, search.getActive())
                        .and(checkUuidIn(TwinFactoryMultiplierEntity.Fields.id, search.getIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryMultiplierEntity.Fields.id, search.getIdExcludeList(), true, false))
                        .and(checkUuidIn(TwinFactoryMultiplierEntity.Fields.twinFactoryId, search.getFactoryIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryMultiplierEntity.Fields.twinFactoryId, search.getFactoryIdExcludeList(), true, false))
                        .and(checkUuidIn(TwinFactoryMultiplierEntity.Fields.inputTwinClassId, search.getInputTwinClassIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryMultiplierEntity.Fields.inputTwinClassId, search.getInputTwinClassIdExcludeList(), true, false))
                        .and(checkIntegerIn(TwinFactoryMultiplierEntity.Fields.inputTwinClassId, search.getMultiplierFeaturerIdList(), false))
                        .and(checkIntegerIn(TwinFactoryMultiplierEntity.Fields.inputTwinClassId, search.getMultiplierFeaturerIdExcludeList(), true))
                        .and(checkFieldLikeIn(TwinFactoryMultiplierEntity.Fields.description, search.getDescriptionLikeList(), false, true))
                        .and(checkFieldLikeIn(TwinFactoryMultiplierEntity.Fields.description, search.getDescriptionNotLikeList(), true, true))
        );
    }
}
