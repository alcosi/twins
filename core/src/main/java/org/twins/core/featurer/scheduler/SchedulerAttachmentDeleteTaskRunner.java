package org.twins.core.featurer.scheduler;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.AttachmentDeleteTaskEntity;
import org.twins.core.dao.attachment.AttachmentDeleteTaskRepository;
import org.twins.core.enums.attachment.AttachmentDeleteTaskStatus;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.scheduler.tasks.AttachmentDeleteTask;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.StreamSupport;

@Service
@Featurer(
        id = FeaturerTwins.ID_5001,
        name = "SchedulerAttachmentDeleteTaskRunner",
        description = "Scheduler for clearing external file storages after twin/attachment deletion"
)
public class SchedulerAttachmentDeleteTaskRunner extends SchedulerTaskRunner<AttachmentDeleteTask, AttachmentDeleteTaskEntity> {

    private final AttachmentDeleteTaskRepository attachmentDeleteTaskRepository;

    protected SchedulerAttachmentDeleteTaskRunner(@Qualifier("attachmentDeleteTaskExecutor") Executor taskExecutor,
                                                  AttachmentDeleteTaskRepository attachmentDeleteTaskRepository) {
        super(taskExecutor);
        this.attachmentDeleteTaskRepository = attachmentDeleteTaskRepository;
    }

    @Override
    protected Class<AttachmentDeleteTask> getTaskClass() {
        return AttachmentDeleteTask.class;
    }

    @Override
    protected Collection<AttachmentDeleteTaskEntity> setStatusAndSave(Collection<AttachmentDeleteTaskEntity> collectedEntities) {
        collectedEntities.forEach(task -> task.setStatus(AttachmentDeleteTaskStatus.IN_PROGRESS));
        return StreamSupport.stream(attachmentDeleteTaskRepository.saveAll(collectedEntities).spliterator(), false).toList();
    }

    @Override
    protected List<AttachmentDeleteTaskEntity> collectAll() {
        return attachmentDeleteTaskRepository.findByStatusIn(List.of(AttachmentDeleteTaskStatus.NEED_START));
    }

    @Override
    protected List<AttachmentDeleteTaskEntity> collectBatch(int batchSize) {
        return attachmentDeleteTaskRepository.findByStatusIn(List.of(AttachmentDeleteTaskStatus.NEED_START), PageRequest.of(0, batchSize));
    }
}
