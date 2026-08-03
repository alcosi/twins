package org.twins.core.featurer.scheduler;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
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
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.notification.HistoryNotificationTaskService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

@Service
@Featurer(
        id = FeaturerTwins.ID_5008,
        name = "SchedulerHistoryNotificationTaskRunner",
        description = "Scheduler for history notifications sending"
)
public class SchedulerHistoryNotificationTaskRunner extends SchedulerTaskRunner<HistoryNotificationTask, HistoryNotificationTaskEntity> {

    private final HistoryNotificationTaskRepository historyNotificationTaskRepository;
    private final HistoryService historyService;
    private final HistoryNotificationTaskService historyNotificationTaskService;

    @Autowired
    public SchedulerHistoryNotificationTaskRunner(@Qualifier("historyNotificationTaskExecutor") Executor taskExecutor,
                                                  HistoryNotificationTaskRepository historyNotificationTaskRepository,
                                                  HistoryService historyService,
                                                  HistoryNotificationTaskService historyNotificationTaskService) {
        super(taskExecutor);
        this.historyNotificationTaskRepository = historyNotificationTaskRepository;
        this.historyService = historyService;
        this.historyNotificationTaskService = historyNotificationTaskService;
    }

    @Override
    protected Class<HistoryNotificationTask> getTaskClass() {
        return HistoryNotificationTask.class;
    }

    @Override
    protected Collection<HistoryNotificationTaskEntity> setStatusAndSave(Collection<HistoryNotificationTaskEntity> collectedEntities) {
        try {
            // order matters: history must be populated before loadHistoryActors reads task.getHistory()
            historyNotificationTaskService.loadHistory(collectedEntities);
            loadHistoryActors(collectedEntities);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
        collectedEntities.forEach(task -> task.setStatusId(HistoryNotificationTaskStatus.IN_PROGRESS));
        historyNotificationTaskRepository.saveAll(collectedEntities);
        return collectedEntities;
    }

    private void loadHistoryActors(Collection<HistoryNotificationTaskEntity> tasks) throws ServiceException {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        var historyEntities = tasks.stream().map(HistoryNotificationTaskEntity::getHistory).toList();
        historyService.loadUser(historyEntities);
        historyService.loadTwin(historyEntities);
    }

    @Override
    protected void revertStatusAndSave(Collection<HistoryNotificationTaskEntity> entities) {
        entities.forEach(entity -> entity.setStatusId(HistoryNotificationTaskStatus.NEED_START));
        historyNotificationTaskRepository.saveAll(entities);
    }

    @Override
    protected List<HistoryNotificationTaskEntity> collectAll() {
        var historyTasks = historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START));
        if (CollectionUtils.isEmpty(historyTasks)) {
            return Collections.emptyList();
        }
        return historyTasks;
    }

    @Override
    protected List<HistoryNotificationTaskEntity> collectBatch(int batchSize) {
        return historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START), PageRequest.of(0, batchSize));
    }
}
