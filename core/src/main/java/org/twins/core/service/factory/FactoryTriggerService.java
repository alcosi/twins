package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dao.trigger.TwinFactoryTriggerRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class FactoryTriggerService extends EntitySecureFindServiceImpl<TwinFactoryTriggerEntity> {
    private final TwinFactoryTriggerRepository repository;

    @Override
    public CrudRepository<TwinFactoryTriggerEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinFactoryTriggerEntity, UUID> entityGetIdFunction() {
        return TwinFactoryTriggerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryTriggerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        //todo
        return false;
    }

    @Override
    public boolean validateEntity(TwinFactoryTriggerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        //todo
        return true;
    }
}
