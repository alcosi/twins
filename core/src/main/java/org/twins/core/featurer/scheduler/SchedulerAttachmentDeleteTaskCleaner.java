package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.AttachmentDeleteTaskRepository;
import org.twins.core.enums.attachment.AttachmentDeleteTaskStatus;
import org.twins.core.featurer.FeaturerTwins;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
@Featurer(
        id = FeaturerTwins.ID_5007,
        name = "SchedulerAttachmentDeleteTaskCleaner",
        description = "Scheduler for cleaning attachment delete task table"
)
public class SchedulerAttachmentDeleteTaskCleaner extends SchedulerCleaner {

    private final AttachmentDeleteTaskRepository attachmentDeleteTaskRepository;

    @Override
    protected void deleteAll() {
        attachmentDeleteTaskRepository.deleteAllByStatusIn(List.of(AttachmentDeleteTaskStatus.DONE));
    }

    @Override
    protected long countAll() {
        return attachmentDeleteTaskRepository.countAllByStatusIn(List.of(AttachmentDeleteTaskStatus.DONE));
    }

    @Override
    protected void deleteAllByCreatedAtBefore(Timestamp createdBefore) {
        attachmentDeleteTaskRepository.deleteAllByStatusInAndCreatedAtBefore(List.of(AttachmentDeleteTaskStatus.DONE), createdBefore);
    }

    @Override
    protected long countAllByCreatedAtBefore(Timestamp createdBefore) {
        return attachmentDeleteTaskRepository.countAllByStatusInAndCreatedAtBefore(List.of(AttachmentDeleteTaskStatus.DONE), createdBefore);
    }
}
