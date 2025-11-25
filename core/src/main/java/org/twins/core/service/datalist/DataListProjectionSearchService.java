package org.twins.core.service.datalist;

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
import org.twins.core.dao.datalist.*;
import org.twins.core.domain.search.DataListProjectionSearch;

import static org.twins.core.dao.specifications.CommonSpecification.*;

// Log calls that took more than 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class DataListProjectionSearchService {
    private final DataListProjectionRepository dataListProjectionRepository;

    public PaginationResult<DataListProjectionEntity> findDataListProjections(DataListProjectionSearch search, SimplePagination pagination) throws ServiceException {
        Specification<DataListProjectionEntity> spec = createSpecification(search);
        Page<DataListProjectionEntity> ret = dataListProjectionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<DataListProjectionEntity> createSpecification(DataListProjectionSearch search) throws ServiceException {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, DataListOptionProjectionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, DataListOptionProjectionEntity.Fields.id),
                checkUuidIn(search.getSrcDataListIdList(), false, false, DataListOptionProjectionEntity.Fields.srcDataListOptionId),
                checkUuidIn(search.getSrcDataListIdExcludeList(), true, false, DataListOptionProjectionEntity.Fields.srcDataListOptionId),
                checkUuidIn(search.getDstDataListIdList(), false, false, DataListOptionProjectionEntity.Fields.dstDataListOptionId),
                checkUuidIn(search.getDstDataListIdExcludeList(), true, false, DataListOptionProjectionEntity.Fields.dstDataListOptionId),
                checkUuidIn(search.getSavedByUserIdList(), false, false, DataListOptionProjectionEntity.Fields.savedByUserId),
                checkUuidIn(search.getSavedByUserIdExcludeList(), true, false, DataListOptionProjectionEntity.Fields.savedByUserId),
                checkFieldLikeIn(search.getNameLikeList(), false, true, DataListProjectionEntity.Fields.name),
                checkFieldLikeIn(search.getNameNotLikeList(), true, true, DataListProjectionEntity.Fields.name),
                checkFieldLocalDateTimeBetween(search.getChangedAt(), DataListProjectionEntity.Fields.changedAt)
        );
    }
}
