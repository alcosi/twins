package org.twins.core.service.scheduler;

import lombok.RequiredArgsConstructor;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.scheduler.SchedulerLogEntity;
import org.twins.core.dao.scheduler.SchedulerLogRepository;
import org.twins.core.domain.search.SchedulerLogSearch;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.LongRangeDTOReverseMapper;

import static org.springframework.data.jpa.domain.Specification.allOf;
import static org.twins.core.dao.specifications.CommonSpecification.*;

@Service
@RequiredArgsConstructor
public class SchedulerLogSearchService {

    private final DataTimeRangeDTOReverseMapper dateMapper;
    private final SchedulerLogRepository schedulerLogRepository;
    private final LongRangeDTOReverseMapper longRangeMapper;

    public PaginationResult<SchedulerLogEntity> search(SchedulerLogSearch schedulerLogSearch, SimplePagination pagination) throws Exception {
        if (schedulerLogSearch == null) {
            schedulerLogSearch = new SchedulerLogSearch();
        }
        Page<SchedulerLogEntity> schedulerList = schedulerLogRepository.findAll(createSchedulerLogEntitySearchSpecification(schedulerLogSearch), PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(schedulerList, pagination);
    }

    private Specification<SchedulerLogEntity> createSchedulerLogEntitySearchSpecification(SchedulerLogSearch search) throws Exception {
        return allOf(
                checkUuidIn(search.getIdSet(), false, false, SchedulerLogEntity.Fields.id),
                checkUuidIn(search.getIdExcludeSet(), true, false, SchedulerLogEntity.Fields.id),
                checkUuidIn(search.getSchedulerIdSet(), false, false, SchedulerLogEntity.Fields.schedulerId),
                checkUuidIn(search.getSchedulerIdExcludeSet(), true, false, SchedulerLogEntity.Fields.schedulerId),
                checkFieldLikeIn(search.getResultLikeSet(), false, false, SchedulerLogEntity.Fields.result),
                checkFieldLikeIn(search.getResultNotLikeSet(), true, true, SchedulerLogEntity.Fields.result),
                checkFieldLocalDateTimeBetween(search.getCreatedAt(), SchedulerLogEntity.Fields.createdAt),
                checkFieldLongRange(longRangeMapper.convert(search.getExecutionTimeRange()), SchedulerLogEntity.Fields.executionTime)
        );
    }
}
