package org.twins.core.service.twinclass;

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
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerEntity;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerRepository;
import org.twins.core.domain.search.TwinClassDynamicMarkerSearch;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassDynamicMarkerSearchService {

    private final TwinClassDynamicMarkerRepository twinClassDynamicMarkerRepository;

    public PaginationResult<TwinClassDynamicMarkerEntity> findTwinClassDynamicMarkers(TwinClassDynamicMarkerSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinClassDynamicMarkerEntity> spec = createTwinClassDynamicMarkerSearchSpecification(search);
        Page<TwinClassDynamicMarkerEntity> ret = twinClassDynamicMarkerRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinClassDynamicMarkerEntity> createTwinClassDynamicMarkerSearchSpecification(TwinClassDynamicMarkerSearch search) {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinClassDynamicMarkerEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinClassDynamicMarkerEntity.Fields.id),
                checkUuidIn(search.getTwinClassIdList(), false, false, TwinClassDynamicMarkerEntity.Fields.twinClassId),
                checkUuidIn(search.getTwinClassIdExcludeList(), true, false, TwinClassDynamicMarkerEntity.Fields.twinClassId),
                checkUuidIn(search.getTwinValidatorSetIdList(), false, false, TwinClassDynamicMarkerEntity.Fields.twinValidatorSetId),
                checkUuidIn(search.getTwinValidatorSetIdExcludeList(), true, false, TwinClassDynamicMarkerEntity.Fields.twinValidatorSetId),
                checkUuidIn(search.getMarkerDataListOptionIdList(), false, false, TwinClassDynamicMarkerEntity.Fields.markerDataListOptionId),
                checkUuidIn(search.getMarkerDataListOptionIdExcludeList(), true, false, TwinClassDynamicMarkerEntity.Fields.markerDataListOptionId)
        );
    }
}
