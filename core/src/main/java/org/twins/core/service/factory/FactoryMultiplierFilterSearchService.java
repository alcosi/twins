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
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterRepository;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.domain.search.FactoryMultiplierFilterSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.factory.FactoryMultiplierFilterSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryMultiplierFilterSearchService {
    private final TwinFactoryMultiplierFilterRepository twinFactoryMultiplierFilterRepository;
    private final AuthService authService;

    public PaginationResult<TwinFactoryMultiplierFilterEntity> findFactoryMultiplierFilters(FactoryMultiplierFilterSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryMultiplierFilterEntity> spec = createFactoryMultiplierFilterSearchSpecification(search);
        Page<TwinFactoryMultiplierFilterEntity> ret = twinFactoryMultiplierFilterRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryMultiplierFilterEntity> createFactoryMultiplierFilterSearchSpecification(FactoryMultiplierFilterSearch search) throws ServiceException {
        return Specification.allOf(
                checkDomainId(authService.getApiUser().getDomainId()),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinFactoryPipelineEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryPipelineEntity.Fields.description),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryMultiplierFilterEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryMultiplierFilterEntity.Fields.id),
                checkFactoryIdIn(search.getFactoryIdList(), false),
                checkFactoryIdIn(search.getFactoryIdExcludeList(), true),
                checkUuidIn(search.getFactoryMultiplierIdList(), false, false, TwinFactoryMultiplierFilterEntity.Fields.twinFactoryMultiplierId),
                checkUuidIn(search.getFactoryMultiplierIdExcludeList(), true, false, TwinFactoryMultiplierFilterEntity.Fields.twinFactoryMultiplierId),
                checkUuidIn(search.getInputTwinClassIdList(), false, false, TwinFactoryMultiplierFilterEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getInputTwinClassIdExcludeList(), true, false, TwinFactoryMultiplierFilterEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getFactoryConditionSetIdList(), false, false, TwinFactoryMultiplierFilterEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getFactoryConditionSetIdExcludeList(), true, true, TwinFactoryMultiplierFilterEntity.Fields.twinFactoryConditionSetId),
                checkTernary(search.getActive(), TwinFactoryMultiplierFilterEntity.Fields.active),
                checkTernary(search.getFactoryConditionInvert(), TwinFactoryMultiplierFilterEntity.Fields.twinFactoryConditionInvert));
    }
}
