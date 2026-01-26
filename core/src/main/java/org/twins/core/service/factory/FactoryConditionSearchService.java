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
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.dao.factory.TwinFactoryConditionRepository;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.domain.search.FactoryConditionSearch;

import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.specifications.factory.FactoryConditionSpecification.checkFieldConditionerFeaturerIdIn;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryConditionSearchService {
    private final TwinFactoryConditionRepository twinFactoryConditionRepository;

    public PaginationResult<TwinFactoryConditionEntity> findFactoryConditions(FactoryConditionSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryConditionEntity> spec = createFactoryConditionSearchSpecification(search);
        Page<TwinFactoryConditionEntity> ret = twinFactoryConditionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryConditionEntity> createFactoryConditionSearchSpecification(FactoryConditionSearch search) throws ServiceException {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinFactoryConditionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryConditionEntity.Fields.id),
                checkUuidIn(search.getFactoryConditionSetIdList(), false, false, TwinFactoryConditionEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getFactoryConditionSetIdExcludeList(), true, false, TwinFactoryConditionEntity.Fields.twinFactoryConditionSetId),
                checkFieldConditionerFeaturerIdIn(search.getConditionerFeaturerIdList(), false),
                checkFieldConditionerFeaturerIdIn(search.getConditionerFeaturerIdExcludeList(), true),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinFactoryConditionSetEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryConditionSetEntity.Fields.description),
                checkTernary(search.getInvert(),TwinFactoryConditionEntity.Fields.invert),
                checkTernary(search.getActive(), TwinFactoryConditionEntity.Fields.active));
    }
}
