package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Service;
import org.twins.core.dao.notification.HistoryNotificationTaskRepository;
import org.twins.core.enums.HistoryNotificationTaskStatus;
import org.twins.core.featurer.FeaturerTwins;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
@Featurer(
        id = FeaturerTwins.ID_5009,
        name = "SchedulerHistoryNotificationTaskCleaner",
        description = "Scheduler for cleaning history notification task table"
)
public class SchedulerHistoryNotificationTaskCleaner extends SchedulerCleaner {

    private final HistoryNotificationTaskRepository historyNotificationTaskRepository;

    @Override
    protected void deleteAll() {
        historyNotificationTaskRepository.deleteAllByStatusIdIn(List.of(HistoryNotificationTaskStatus.SENT));
    }

    @Override
    protected long countAll() {
        return historyNotificationTaskRepository.countAllByStatusIdIn(List.of(HistoryNotificationTaskStatus.SENT));
    }

    @Override
    protected void deleteAllByCreatedAtBefore(Timestamp createdBefore) {
        historyNotificationTaskRepository.deleteAllByStatusIdInAndCreatedAtBefore(List.of(HistoryNotificationTaskStatus.SENT), createdBefore);
    }

    @Override
    protected long countAllByCreatedAtBefore(Timestamp createdBefore) {
        return historyNotificationTaskRepository.countAllByStatusIdInAndCreatedAtBefore(List.of(HistoryNotificationTaskStatus.SENT), createdBefore);
    }
}
