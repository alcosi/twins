package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinArchiveRepository;
import org.twins.core.featurer.FeaturerTwins;

import java.sql.Timestamp;

@RequiredArgsConstructor
@Service
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_5002,
        name = "SchedulerTwinArchiveCleaner",
        description = "Scheduler for clearing twin archive table"
)
public class SchedulerTwinArchiveCleaner extends SchedulerCleaner {

    private final TwinArchiveRepository twinArchiveRepository;

    @Override
    protected void deleteAll() {
        twinArchiveRepository.deleteAll();
    }

    @Override
    protected long countAll() {
        return twinArchiveRepository.count();
    }

    @Override
    protected void deleteAllByCreatedAtBefore(Timestamp createdBefore) {
        twinArchiveRepository.deleteAllByCreatedAtBefore(createdBefore);
    }

    @Override
    protected long countAllByCreatedAtBefore(Timestamp createdBefore) {
        return twinArchiveRepository.countAllByCreatedAtBefore(createdBefore);
    }
}
