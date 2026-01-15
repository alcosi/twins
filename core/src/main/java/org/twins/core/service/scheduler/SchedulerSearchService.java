package org.twins.core.service.scheduler;

import lombok.RequiredArgsConstructor;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dao.scheduler.SchedulerRepository;
import org.twins.core.domain.search.SchedulerSearch;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.IntegerRangeDTOReverseMapper;

import static org.springframework.data.jpa.domain.Specification.allOf;
import static org.twins.core.dao.specifications.CommonSpecification.*;

@Service
@RequiredArgsConstructor
public class SchedulerSearchService {

    private final SchedulerRepository schedulerRepository;
    private final DataTimeRangeDTOReverseMapper dateMapper;
    private final IntegerRangeDTOReverseMapper integerRangeMapper;

    public PaginationResult<SchedulerEntity> search(SchedulerSearch schedulerSearch, SimplePagination pagination) throws Exception {
        if (schedulerSearch == null) {
            schedulerSearch = new SchedulerSearch();
        }
        Page<SchedulerEntity> schedulerList = schedulerRepository.findAll(createSchedulerEntitySearchSpecification(schedulerSearch), PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(schedulerList, pagination);
    }

    private Specification<SchedulerEntity> createSchedulerEntitySearchSpecification(SchedulerSearch search) throws Exception {
        return allOf(
                checkUuidIn(search.getIdSet(), false, false, SchedulerEntity.Fields.id),
                checkUuidIn(search.getIdExcludeSet(), true, false, SchedulerEntity.Fields.id),
                checkUuidIn(search.getDomainIdSet(), false, false, SchedulerEntity.Fields.domainId),
                checkUuidIn(search.getDomainIdExcludeSet(), true, false, SchedulerEntity.Fields.domainId),
                checkIntegerIn(search.getFeaturerIdSet(), false, SchedulerEntity.Fields.schedulerFeaturerId),
                checkIntegerIn(search.getFeaturerIdExcludeSet(), true, SchedulerEntity.Fields.schedulerFeaturerId),
                checkTernary(search.getActive(), SchedulerEntity.Fields.active),
                checkTernary(search.getLogEnabled(), SchedulerEntity.Fields.logEnabled),
                checkFieldLikeIn(search.getCronSet(), false, false, SchedulerEntity.Fields.cron),
                checkFieldLikeIn(search.getCronExcludeSet(), true, true, SchedulerEntity.Fields.cron),
                checkFieldIntegerRange(integerRangeMapper.convert(search.getFixedRateRange()), SchedulerEntity.Fields.fixedRate),
                checkFieldLikeIn(search.getDescriptionLikeSet(), false, false, SchedulerEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeSet(), true, true, SchedulerEntity.Fields.description),
                checkFieldLocalDateTimeBetween(dateMapper.convert(search.getCreatedAtRange()), SchedulerEntity.Fields.createdAt),
                checkFieldLocalDateTimeBetween(dateMapper.convert(search.getUpdatedAtRange()), SchedulerEntity.Fields.updatedAt)
        );
    }
}
