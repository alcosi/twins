package org.twins.core.service.attachment;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.TaskStatus;
import org.twins.core.dao.attachment.AttachmentDeleteTaskEntity;
import org.twins.core.dao.attachment.AttachmentDeleteTaskRepository;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.featurer.storager.Storager;

@Component
@Scope("prototype")
@Slf4j
public class AttachmentDeleteTask implements Runnable {

    private final AttachmentDeleteTaskEntity attachmentDeleteTaskEntity;

    @Autowired
    private FeaturerService featurerService;
    @Autowired
    private AttachmentDeleteTaskRepository attachmentDeleteTaskRepository;

    @Autowired
    public AttachmentDeleteTask(AttachmentDeleteTaskEntity attachmentDeleteTaskEntity) {
        this.attachmentDeleteTaskEntity = attachmentDeleteTaskEntity;
    }

    @Override
    @SneakyThrows
    public void run() {
        try {
            LoggerUtils.logController("attachmentDeleteTask$");
            LoggerUtils.logPrefix("ATTACHMENT_DELETE_TASK[" + attachmentDeleteTaskEntity.getId() + "]:");
            StorageEntity storage = attachmentDeleteTaskEntity.getStorage();
            Storager fileService = featurerService.getFeaturer(storage.getStorageFeaturer(), Storager.class);
            fileService.tryDeleteFile(attachmentDeleteTaskEntity.getStorageFileKey(), storage.getStoragerParams());
            attachmentDeleteTaskEntity.setStatus(TaskStatus.DONE);
        } catch (ServiceException e) {
            log.error(e.log());
            attachmentDeleteTaskEntity.setStatus(TaskStatus.FAILED);
        } catch (Throwable e) {
            log.error("Exception: ", e);
            attachmentDeleteTaskEntity.setStatus(TaskStatus.FAILED);
        } finally {
            LoggerUtils.cleanMDC();
            attachmentDeleteTaskRepository.save(attachmentDeleteTaskEntity);
        }
    }
}
