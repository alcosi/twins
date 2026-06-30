package org.twins.core.service.notification;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.notification.NotificationChannelEntity;
import org.twins.core.dao.notification.NotificationChannelRepository;
import org.twins.core.service.TwinsEntitySecureFindService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class NotificationChannelService extends TwinsEntitySecureFindService<NotificationChannelEntity> {
    @Getter
    private final NotificationChannelRepository repository;

    @Override
    public CrudRepository<NotificationChannelEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<NotificationChannelEntity, UUID> entityGetIdFunction() {
        return NotificationChannelEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(NotificationChannelEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(NotificationChannelEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
