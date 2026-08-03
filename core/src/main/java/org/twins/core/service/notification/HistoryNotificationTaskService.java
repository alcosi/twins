package org.twins.core.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskRepository;
import org.twins.core.service.history.HistoryService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class HistoryNotificationTaskService extends EntitySecureFindServiceImpl<HistoryNotificationTaskEntity> {
    private final HistoryNotificationTaskRepository repository;
    private final HistoryService historyService;
    private final NotificationSchemaService notificationSchemaService;

    @Override
    public CrudRepository<HistoryNotificationTaskEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<HistoryNotificationTaskEntity, UUID> entityGetIdFunction() {
        return HistoryNotificationTaskEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(HistoryNotificationTaskEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(HistoryNotificationTaskEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadHistory(HistoryNotificationTaskEntity entity) throws ServiceException {
        loadHistory(Collections.singleton(entity));
    }

    public void loadHistory(Collection<HistoryNotificationTaskEntity> entities) throws ServiceException {
        historyService.load(entities,
                HistoryNotificationTaskEntity::getHistoryId,
                HistoryNotificationTaskEntity::getHistory,
                HistoryNotificationTaskEntity::setHistory);
    }

    public void loadNotificationSchema(HistoryNotificationTaskEntity entity) throws ServiceException {
        loadNotificationSchema(Collections.singleton(entity));
    }

    public void loadNotificationSchema(Collection<HistoryNotificationTaskEntity> entities) throws ServiceException {
        notificationSchemaService.load(entities,
                HistoryNotificationTaskEntity::getNotificationSchemaId,
                HistoryNotificationTaskEntity::getNotificationSchema,
                HistoryNotificationTaskEntity::setNotificationSchema);
    }
}
