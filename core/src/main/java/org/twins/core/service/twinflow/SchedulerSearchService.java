package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dao.scheduler.SchedulerRepository;
import org.twins.core.domain.search.SchedulerSearch;

import static org.springframework.data.jpa.domain.Specification.allOf;
import static org.twins.core.dao.specifications.scheduler.SchedulerSpecification.*;

@Service
@RequiredArgsConstructor
public class SchedulerSearchService {

    private final SchedulerRepository schedulerRepository;

    public PaginationResult<SchedulerEntity> search(SchedulerSearch schedulerSearch, SimplePagination pagination) throws ServiceException {
        if (schedulerSearch == null) {
            schedulerSearch = new SchedulerSearch();
        }
        Page<SchedulerEntity> schedulerList = schedulerRepository.findAll(createSchedulerEntitySearchSpecification(schedulerSearch), PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(schedulerList, pagination);
    }

    private Specification<SchedulerEntity> createSchedulerEntitySearchSpecification(SchedulerSearch search) throws ServiceException {
        return allOf(checkUuidIn(search.getIdSet(), false, false, SchedulerEntity.Fields.id),
                checkUuidIn(search.getIdExcludeSet(), true, false, SchedulerEntity.Fields.id),
                checkIntegerIn(search.getFeaturerIdSet(), false, SchedulerEntity.Fields.featurerId),
                checkIntegerIn(search.getFeaturerIdExcludeSet(), true, SchedulerEntity.Fields.featurerId),
                checkTernary(search.getActive(), SchedulerEntity.Fields.active),
                checkTernary(search.getLogEnabled(), SchedulerEntity.Fields.logEnabled),
                checkFieldLikeIn(search.getCronSet(), false, false, SchedulerEntity.Fields.cron),
                checkFieldLikeIn(search.getCronExcludeSet(), true, true, SchedulerEntity.Fields.cron),
                checkIntegerIn(search.getFixedRateSet(), false, SchedulerEntity.Fields.fixedRate),
                checkIntegerIn(search.getFixedRateExcludeSet(), true, SchedulerEntity.Fields.fixedRate),
                checkFieldLikeIn(search.getDescriptionSet(), false, false, SchedulerEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionExcludeSet(), true, true, SchedulerEntity.Fields.description),
                checkFieldLocalDateTimeBetween(search.getCreatedAt(), SchedulerEntity.Fields.createdAt),
                checkFieldLocalDateTimeBetween(search.getUpdatedAt(), SchedulerEntity.Fields.updatedAt)
        );
    }
}
