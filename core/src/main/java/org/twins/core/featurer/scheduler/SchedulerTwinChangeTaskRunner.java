package org.twins.core.featurer.scheduler;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.twins.core.dao.TwinChangeTaskStatus;
import org.twins.core.dao.twin.TwinChangeTaskEntity;
import org.twins.core.dao.twin.TwinChangeTaskRepository;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.scheduler.tasks.TwinChangeTask;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.StreamSupport;

@Service
@Featurer(
        id = FeaturerTwins.ID_5003,
        name = "SchedulerTwinChangeTaskRunner",
        description = "Scheduler for executing twin changes"
)
public class SchedulerTwinChangeTaskRunner extends SchedulerTaskRunner<TwinChangeTask, TwinChangeTaskEntity> {

    private final TwinChangeTaskRepository twinChangeTaskRepository;

    protected SchedulerTwinChangeTaskRunner(@Qualifier("twinChangeTaskExecutor") Executor taskExecutor,
                                            TwinChangeTaskRepository twinChangeTaskRepository) {
        super(taskExecutor);
        this.twinChangeTaskRepository = twinChangeTaskRepository;
    }

    @Override
    protected Class<TwinChangeTask> getTaskClass() {
        return TwinChangeTask.class;
    }

    @Override
    protected Collection<TwinChangeTaskEntity> setStatusAndSave(Collection<TwinChangeTaskEntity> collectedEntities) {
        collectedEntities.forEach(task -> task.setStatusId(TwinChangeTaskStatus.IN_PROGRESS));
        return StreamSupport.stream(twinChangeTaskRepository.saveAll(collectedEntities).spliterator(), false).toList();
    }

    @Override
    protected List<TwinChangeTaskEntity> collectAll() {
        return twinChangeTaskRepository.findByStatusIdIn(List.of(TwinChangeTaskStatus.NEED_START));
    }

    @Override
    protected List<TwinChangeTaskEntity> collectBatch(int batchSize) {
        return twinChangeTaskRepository.findByStatusIdIn(List.of(TwinChangeTaskStatus.NEED_START), PageRequest.of(0, batchSize));
    }
}
