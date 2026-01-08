package org.twins.core.service.twinclass;

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
import org.twins.core.dao.twinclass.TwinClassFreezeEntity;
import org.twins.core.dao.twinclass.TwinClassFreezeRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFreezeService extends EntitySecureFindServiceImpl<TwinClassFreezeEntity> {
    private final TwinClassFreezeRepository repository;
    @Override
    public CrudRepository<TwinClassFreezeEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinClassFreezeEntity, UUID> entityGetIdFunction() {
        return TwinClassFreezeEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFreezeEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFreezeEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
