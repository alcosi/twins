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
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierRepository;
import org.twins.core.domain.search.FactoryMultiplierSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.specifications.factory.FactoryMultiplierSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryMultiplierSearchService {
    private final TwinFactoryMultiplierRepository twinFactoryMultiplierRepository;
    private final AuthService authService;

    public PaginationResult<TwinFactoryMultiplierEntity> findFactoryMultipliers(FactoryMultiplierSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryMultiplierEntity> spec = createFactoryMultiplierSearchSpecification(search);
        Page<TwinFactoryMultiplierEntity> ret = twinFactoryMultiplierRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryMultiplierEntity> createFactoryMultiplierSearchSpecification(FactoryMultiplierSearch search) throws ServiceException {
        return Specification.allOf(
                checkDomainId(authService.getApiUser().getDomainId()),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryMultiplierEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryMultiplierEntity.Fields.id),
                checkUuidIn(search.getFactoryIdList(), false, false, TwinFactoryMultiplierEntity.Fields.twinFactoryId),
                checkUuidIn(search.getFactoryIdExcludeList(), true, false, TwinFactoryMultiplierEntity.Fields.twinFactoryId),
                checkUuidIn(search.getInputTwinClassIdList(), false, false, TwinFactoryMultiplierEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getInputTwinClassIdExcludeList(), true, false, TwinFactoryMultiplierEntity.Fields.inputTwinClassId),
                checkIntegerIn(search.getMultiplierFeaturerIdList(), false, TwinFactoryMultiplierEntity.Fields.inputTwinClassId),
                checkIntegerIn(search.getMultiplierFeaturerIdExcludeList(), true, TwinFactoryMultiplierEntity.Fields.inputTwinClassId),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinFactoryMultiplierEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryMultiplierEntity.Fields.description),
                checkTernary(search.getActive(), TwinFactoryMultiplierEntity.Fields.active)
        );
    }
}
