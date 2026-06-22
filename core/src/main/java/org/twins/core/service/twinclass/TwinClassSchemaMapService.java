package org.twins.core.service.twinclass;

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
import org.twins.core.dao.twinclass.TwinClassSchemaMapEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaMapRepository;
import org.twins.core.service.TwinsEntitySecureFindService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassSchemaMapService extends TwinsEntitySecureFindService<TwinClassSchemaMapEntity> {
    @Getter
    private final TwinClassSchemaMapRepository repository;
    @Lazy
    private final TwinClassSchemaService twinClassSchemaService;
    @Lazy
    private final TwinClassService twinClassService;

    @Override
    public CrudRepository<TwinClassSchemaMapEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinClassSchemaMapEntity, UUID> entityGetIdFunction() {
        return TwinClassSchemaMapEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassSchemaMapEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassSchemaMapEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadTwinClassSchema(TwinClassSchemaMapEntity src) throws ServiceException {
        loadTwinClassSchema(Collections.singletonList(src));
    }

    public void loadTwinClassSchema(Collection<TwinClassSchemaMapEntity> srcCollection) throws ServiceException {
        twinClassSchemaService.load(srcCollection,
                TwinClassSchemaMapEntity::getTwinClassSchemaId,
                TwinClassSchemaMapEntity::getTwinClassSchema,
                TwinClassSchemaMapEntity::setTwinClassSchema);
    }

    public void loadTwinClass(TwinClassSchemaMapEntity src) throws ServiceException {
        loadTwinClass(Collections.singletonList(src));
    }

    public void loadTwinClass(Collection<TwinClassSchemaMapEntity> srcCollection) throws ServiceException {
        twinClassService.load(srcCollection,
                TwinClassSchemaMapEntity::getTwinClassId,
                TwinClassSchemaMapEntity::getTwinClass,
                TwinClassSchemaMapEntity::setTwinClass);
    }
}
