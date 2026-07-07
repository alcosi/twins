package org.twins.core.service.notification;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.notification.NotificationChannelEventEntity;
import org.twins.core.dao.notification.NotificationChannelEventRepository;
import org.twins.core.service.TwinsEntitySecureFindService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class NotificationChannelEventService extends TwinsEntitySecureFindService<NotificationChannelEventEntity> {
    @Getter
    private final NotificationChannelEventRepository repository;
    @Lazy
    private final NotificationChannelService notificationChannelService;
    @Lazy
    private final NotificationContextService notificationContextService;

    @Override
    public CrudRepository<NotificationChannelEventEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<NotificationChannelEventEntity, UUID> entityGetIdFunction() {
        return NotificationChannelEventEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(NotificationChannelEventEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(NotificationChannelEventEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadNotificationChannel(NotificationChannelEventEntity src) throws ServiceException {
        loadNotificationChannel(Collections.singletonList(src));
    }

    public void loadNotificationChannel(Collection<NotificationChannelEventEntity> srcCollection) throws ServiceException {
        notificationChannelService.load(srcCollection,
                NotificationChannelEventEntity::getNotificationChannelId,
                NotificationChannelEventEntity::getNotificationChannel,
                NotificationChannelEventEntity::setNotificationChannel);
    }

    public void loadNotificationContext(NotificationChannelEventEntity src) throws ServiceException {
        loadNotificationContext(Collections.singletonList(src));
    }

    public void loadNotificationContext(Collection<NotificationChannelEventEntity> srcCollection) throws ServiceException {
        notificationContextService.load(srcCollection,
                NotificationChannelEventEntity::getNotificationContextId,
                NotificationChannelEventEntity::getNotificationContext,
                NotificationChannelEventEntity::setNotificationContext);
    }
}
