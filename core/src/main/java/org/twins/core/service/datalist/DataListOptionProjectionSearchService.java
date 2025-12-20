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
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.dao.datalist.DataListOptionProjectionRepository;
import org.twins.core.domain.search.DataListOptionProjectionSearch;

import java.util.List;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;

// Log calls that took more than 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class DataListOptionProjectionSearchService {
    private final DataListOptionProjectionRepository dataListOptionProjectionRepository;

    public List<DataListOptionProjectionEntity> findDataListOptionProjections(DataListOptionProjectionSearch search) throws ServiceException {
        Specification<DataListOptionProjectionEntity> spec = createSpecification(search);
        return dataListOptionProjectionRepository.findAll(spec);
    }

    public PaginationResult<DataListOptionProjectionEntity> findDataListOptionProjections(DataListOptionProjectionSearch search, SimplePagination pagination) throws ServiceException {
        Specification<DataListOptionProjectionEntity> spec = createSpecification(search);
        Page<DataListOptionProjectionEntity> page = dataListOptionProjectionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(page, pagination);
    }

    private Specification<DataListOptionProjectionEntity> createSpecification(DataListOptionProjectionSearch search) throws ServiceException {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, DataListOptionProjectionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, DataListOptionProjectionEntity.Fields.id),

                checkUuidIn(search.getProjectionTypeIdList(), false, false, DataListOptionProjectionEntity.Fields.projectionTypeId),
                checkUuidIn(search.getProjectionTypeIdExcludeList(), true, false, DataListOptionProjectionEntity.Fields.projectionTypeId),

                checkUuidIn(search.getSrcDataListOptionIdList(), false, false, DataListOptionProjectionEntity.Fields.srcDataListOptionId),
                checkUuidIn(search.getSrcDataListOptionIdExcludeList(), true, false, DataListOptionProjectionEntity.Fields.srcDataListOptionId),

                checkUuidIn(search.getDstDataListOptionIdList(), false, false, DataListOptionProjectionEntity.Fields.dstDataListOptionId),
                checkUuidIn(search.getDstDataListOptionIdExcludeList(), true, false, DataListOptionProjectionEntity.Fields.dstDataListOptionId),

                checkUuidIn(search.getSavedByUserIdList(), false, false, DataListOptionProjectionEntity.Fields.savedByUserId),
                checkUuidIn(search.getSavedByUserIdExcludeList(), true, false, DataListOptionProjectionEntity.Fields.savedByUserId)
        );
    }
}
