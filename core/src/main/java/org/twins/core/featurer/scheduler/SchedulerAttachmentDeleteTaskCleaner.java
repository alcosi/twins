package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.AttachmentDeleteTaskRepository;
import org.twins.core.enums.attachment.AttachmentDeleteTaskStatus;
import org.twins.core.featurer.FeaturerTwins;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_4707,
        name = "SchedulerAttachmentDeleteTaskCleaner",
        description = "Scheduler for cleaning attachment delete task table"
)
public class SchedulerAttachmentDeleteTaskCleaner extends SchedulerCleaner {

    private final AttachmentDeleteTaskRepository attachmentDeleteTaskRepository;

    @Override
    protected void deleteAll() {
        attachmentDeleteTaskRepository.deleteAll();
    }

    @Override
    protected long countAll() {
        return attachmentDeleteTaskRepository.count();
    }

    @Override
    protected void deleteAllByCreatedAtAfter(Timestamp createdAfter) {
        attachmentDeleteTaskRepository.deleteAllByStatusInAndCreatedAtAfter(List.of(AttachmentDeleteTaskStatus.DONE), createdAfter);
    }

    @Override
    protected long countAllByCreatedAtAfter(Timestamp createdAfter) {
        return attachmentDeleteTaskRepository.countAllByStatusInAndCreatedAtAfter(List.of(AttachmentDeleteTaskStatus.DONE), createdAfter);
    }
}
