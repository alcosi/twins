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
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.factory.TwinFactoryBranchRepository;
import org.twins.core.domain.search.FactoryBranchSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.specifications.factory.FactoryBranchSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryBranchSearchService {
    private final TwinFactoryBranchRepository twinFactoryBranchRepository;
    private final AuthService authService;

    public PaginationResult<TwinFactoryBranchEntity> findFactoryBranches(FactoryBranchSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryBranchEntity> spec = createFactoryBranchSearchSpecification(search);
        Page<TwinFactoryBranchEntity> ret = twinFactoryBranchRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryBranchEntity> createFactoryBranchSearchSpecification(FactoryBranchSearch search) throws ServiceException {
        return Specification.allOf(
                checkDomainId(authService.getApiUser().getDomainId()),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinFactoryBranchEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryBranchEntity.Fields.description),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryBranchEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryBranchEntity.Fields.id),
                checkUuidIn(search.getFactoryIdList(), false, false, TwinFactoryBranchEntity.Fields.twinFactoryId),
                checkUuidIn(search.getFactoryIdExcludeList(), true, false, TwinFactoryBranchEntity.Fields.twinFactoryId),
                checkUuidIn(search.getFactoryConditionSetIdList(), false, false, TwinFactoryBranchEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getFactoryConditionSetIdExcludeList(), true, true, TwinFactoryBranchEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getNextFactoryIdList(), false, false, TwinFactoryBranchEntity.Fields.nextTwinFactoryId),
                checkUuidIn(search.getNextFactoryIdExcludeList(), true, false, TwinFactoryBranchEntity.Fields.nextTwinFactoryId),
                checkTernary(search.getConditionInvert(), TwinFactoryBranchEntity.Fields.twinFactoryConditionInvert),
                checkTernary(search.getActive(), TwinFactoryBranchEntity.Fields.active));
    }
}
