package org.twins.core.service.notification;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.notification.NotificationChannelEventEntity;
import org.twins.core.dao.notification.NotificationChannelEventRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class NotificationEventServiceService extends EntitySecureFindServiceImpl<NotificationChannelEventEntity> {

    private final NotificationChannelEventRepository repository;

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
}
