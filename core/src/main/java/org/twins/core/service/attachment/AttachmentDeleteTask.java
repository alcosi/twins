package org.twins.core.service.attachment;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.AttachmentDeleteTaskEntity;
import org.twins.core.dao.attachment.AttachmentDeleteTaskRepository;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.enums.attachment.AttachmentDeleteTaskStatus;
import org.twins.core.featurer.storager.Storager;
import org.twins.core.service.auth.AuthService;

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
    private AuthService authService;

    @Autowired
    public AttachmentDeleteTask(AttachmentDeleteTaskEntity attachmentDeleteTaskEntity) {
        this.attachmentDeleteTaskEntity = attachmentDeleteTaskEntity;
    }

    @Override
    @SneakyThrows
    public void run() {
        try {
            LoggerUtils.logController("attachmentDeleteTask$");
            LoggerUtils.logPrefix(STR."ATTACHMENT_DELETE_TASK[\{attachmentDeleteTaskEntity.getId()}]:");
            authService.setThreadLocalApiUser(attachmentDeleteTaskEntity.getDomainId(), attachmentDeleteTaskEntity.getTwinOwnerBusinessAccountId(), attachmentDeleteTaskEntity.getTwinCreatedByUserId());
            StorageEntity storage = attachmentDeleteTaskEntity.getStorage();
            Storager fileService = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
            fileService.tryDeleteFile(attachmentDeleteTaskEntity.getStorageFileKey(), storage.getStoragerParams());
            attachmentDeleteTaskEntity.setStatus(AttachmentDeleteTaskStatus.DONE);
        } catch (ServiceException e) {
            log.error(e.log());
            attachmentDeleteTaskEntity.setStatus(AttachmentDeleteTaskStatus.FAILED);
        } catch (Throwable e) {
            log.error("Exception: ", e);
            attachmentDeleteTaskEntity.setStatus(AttachmentDeleteTaskStatus.FAILED);
        } finally {
            LoggerUtils.cleanMDC();
            authService.removeThreadLocalApiUser();
            attachmentDeleteTaskRepository.save(attachmentDeleteTaskEntity);
        }
    }
}
