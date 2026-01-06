package org.twins.core.service.twinflow;

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
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dao.twinflow.TwinflowFactoryRepository;
import org.twins.core.domain.search.TwinflowFactorySearch;

import static org.twins.core.dao.specifications.twinflow.TwinflowFactorySpecification.checkFieldLikeIn;
import static org.twins.core.dao.specifications.twinflow.TwinflowFactorySpecification.checkUuidIn;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinflowFactorySearchService {
    private final TwinflowFactoryRepository twinflowFactoryRepository;

    public PaginationResult<TwinflowFactoryEntity> findTwinflowFactory(TwinflowFactorySearch search, SimplePagination pagination) throws ServiceException {
        Page<TwinflowFactoryEntity> ret = twinflowFactoryRepository.findAll(createTwinflowFactorySearchSpecification(search), PaginationUtils.pageableOffset(pagination));

        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinflowFactoryEntity> createTwinflowFactorySearchSpecification(TwinflowFactorySearch search) {
        return Specification.allOf(
                checkUuidIn(search.getIdSet(), false, false, TwinflowFactoryEntity.Fields.id),
                checkUuidIn(search.getIdExcludeSet(), true, true, TwinflowFactoryEntity.Fields.id),
                checkUuidIn(search.getTwinflowIdSet(), false, false, TwinflowFactoryEntity.Fields.twinflowId),
                checkUuidIn(search.getTwinflowIdExcludeSet(), true, true, TwinflowFactoryEntity.Fields.twinflowId),
                checkUuidIn(search.getTwinFactoryIdSet(), false, false, TwinflowFactoryEntity.Fields.twinFactoryId),
                checkUuidIn(search.getTwinFactoryIdExcludeSet(), true, true, TwinflowFactoryEntity.Fields.twinFactoryId),
                checkFieldLikeIn(search.getFactoryLauncherSet(), false, false, TwinflowFactoryEntity.Fields.twinFactoryLauncher),
                checkFieldLikeIn(search.getFactoryLauncherExcludeSet(), true, true, TwinflowFactoryEntity.Fields.twinFactoryLauncher));
    }
}
