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
import org.twins.core.dao.notification.HistoryNotificationSchemaMapEntity;
import org.twins.core.dao.notification.HistoryNotificationSchemaMapRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class HistoryNotificationSchemaMapService extends EntitySecureFindServiceImpl<HistoryNotificationSchemaMapEntity> {

    private final HistoryNotificationSchemaMapRepository repository;

    @Override
    public CrudRepository<HistoryNotificationSchemaMapEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<HistoryNotificationSchemaMapEntity, UUID> entityGetIdFunction() {
        return HistoryNotificationSchemaMapEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(HistoryNotificationSchemaMapEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(HistoryNotificationSchemaMapEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
