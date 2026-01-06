package org.twins.core.service.twinclass;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassFieldAttributeEntity;
import org.twins.core.dao.twinclass.TwinClassFieldAttributeRepository;

import java.util.UUID;
import java.util.function.Function;

@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFieldAttributeService extends EntitySecureFindServiceImpl<TwinClassFieldAttributeEntity> {
    private final TwinClassFieldAttributeRepository twinClassFieldAttributeRepository;

    @Override
    public CrudRepository<TwinClassFieldAttributeEntity, UUID> entityRepository() {
        return twinClassFieldAttributeRepository;
    }

    @Override
    public Function<TwinClassFieldAttributeEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldAttributeEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldAttributeEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFieldAttributeEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
