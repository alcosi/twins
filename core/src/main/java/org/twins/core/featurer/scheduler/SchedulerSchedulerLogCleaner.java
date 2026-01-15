package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Service;
import org.twins.core.dao.scheduler.SchedulerLogRepository;
import org.twins.core.featurer.FeaturerTwins;

import java.sql.Timestamp;

@RequiredArgsConstructor
@Service
@Featurer(
        id = FeaturerTwins.ID_5006,
        name = "SchedulerSchedulerLogCleaner",
        description = "Scheduler for cleaning scheduler log table"
)
public class SchedulerSchedulerLogCleaner extends SchedulerCleaner {

    private final SchedulerLogRepository schedulerLogRepository;


    @Override
    protected void deleteAll() {
        schedulerLogRepository.deleteAll();
    }

    @Override
    protected long countAll() {
        return schedulerLogRepository.count();
    }

    @Override
    protected void deleteAllByCreatedAtBefore(Timestamp createdBefore) {
        schedulerLogRepository.deleteAllByCreatedAtBefore(createdBefore);
    }

    @Override
    protected long countAllByCreatedAtBefore(Timestamp createdBefore) {
        return schedulerLogRepository.countAllByCreatedAtBefore(createdBefore);
    }
}
