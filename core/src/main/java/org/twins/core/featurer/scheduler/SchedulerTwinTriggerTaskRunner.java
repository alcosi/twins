package org.twins.core.featurer.scheduler;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskRepository;
import org.twins.core.dao.trigger.TwinTriggerTaskStatus;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.scheduler.tasks.TwinTriggerTask;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.StreamSupport;

@Service
@Featurer(
        id = FeaturerTwins.ID_5010,
        name = "SchedulerTwinTriggerTaskRunner",
        description = "Scheduler for executing twin triggers"
)
public class SchedulerTwinTriggerTaskRunner extends SchedulerTaskRunner<TwinTriggerTask, TwinTriggerTaskEntity> {

    private final TwinTriggerTaskRepository twinTriggerTaskRepository;

    public SchedulerTwinTriggerTaskRunner(@Qualifier("twinTriggerTaskExecutor") Executor taskExecutor,
                                          TwinTriggerTaskRepository twinTriggerTaskRepository) {
        super(taskExecutor);
        this.twinTriggerTaskRepository = twinTriggerTaskRepository;
    }

    @Override
    protected Class<TwinTriggerTask> getTaskClass() {
        return TwinTriggerTask.class;
    }

    @Override
    protected Collection<TwinTriggerTaskEntity> setStatusAndSave(Collection<TwinTriggerTaskEntity> collectedEntities) {
        collectedEntities.forEach(task -> task.setStatusId(TwinTriggerTaskStatus.IN_PROGRESS));
        return StreamSupport.stream(twinTriggerTaskRepository.saveAll(collectedEntities).spliterator(), false).toList();
    }

    @Override
    protected List<TwinTriggerTaskEntity> collectAll() {
        return List.copyOf(twinTriggerTaskRepository.findByStatusIdIn(List.of(TwinTriggerTaskStatus.NEED_START)));
    }

    @Override
    protected List<TwinTriggerTaskEntity> collectBatch(int batchSize) {
        return twinTriggerTaskRepository.findByStatusIdIn(List.of(TwinTriggerTaskStatus.NEED_START), PageRequest.of(0, batchSize)).getContent();
    }
}
