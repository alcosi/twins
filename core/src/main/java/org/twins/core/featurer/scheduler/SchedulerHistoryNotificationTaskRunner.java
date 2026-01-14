package org.twins.core.featurer.scheduler;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskRepository;
import org.twins.core.enums.HistoryNotificationTaskStatus;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.scheduler.tasks.HistoryNotificationTask;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.StreamSupport;

@Service
@Featurer(
        id = FeaturerTwins.ID_5008,
        name = "SchedulerHistoryNotificationTaskRunner",
        description = "Scheduler for history notifications sending"
)
public class SchedulerHistoryNotificationTaskRunner extends SchedulerTaskRunner<HistoryNotificationTask, HistoryNotificationTaskEntity> {

    private final HistoryNotificationTaskRepository historyNotificationTaskRepository;

    @Autowired
    public SchedulerHistoryNotificationTaskRunner(@Qualifier("historyNotificationTaskExecutor") Executor taskExecutor,
                                                  HistoryNotificationTaskRepository historyNotificationTaskRepository) {
        super(taskExecutor);
        this.historyNotificationTaskRepository = historyNotificationTaskRepository;
    }

    @Override
    protected Class<HistoryNotificationTask> getTaskClass() {
        return HistoryNotificationTask.class;
    }

    @Override
    protected Collection<HistoryNotificationTaskEntity> setStatusAndSave(Collection<HistoryNotificationTaskEntity> collectedEntities) {
        collectedEntities.forEach(task -> task.setStatusId(HistoryNotificationTaskStatus.IN_PROGRESS));
        return StreamSupport.stream(historyNotificationTaskRepository.saveAll(collectedEntities).spliterator(), false).toList();
    }

    @Override
    protected List<HistoryNotificationTaskEntity> collectAll() {
        return historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START));
    }

    @Override
    protected List<HistoryNotificationTaskEntity> collectBatch(int batchSize) {
        return historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START), PageRequest.of(0, batchSize));
    }
}
