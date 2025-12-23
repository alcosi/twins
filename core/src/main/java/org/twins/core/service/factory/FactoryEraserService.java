package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dao.factory.TwinFactoryEraserRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class FactoryEraserService extends EntitySecureFindServiceImpl<TwinFactoryEraserEntity> {
    @Getter
    private final TwinFactoryEraserRepository repository;
    private final AuthService authService;
    private final TwinFactoryService twinFactoryService;
    private final TwinClassService twinClassService;

    @Override
    public CrudRepository<TwinFactoryEraserEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinFactoryEraserEntity, UUID> entityGetIdFunction() {
        return TwinFactoryEraserEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryEraserEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getTwinFactory().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(TwinFactoryEraserEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinFactoryId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinFactoryId");
        if (entity.getInputTwinClassId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty inputTwinClassId");
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getTwinFactory() == null || !entity.getTwinFactory().getId().equals(entity.getTwinFactoryId()))
                    entity.setTwinFactory(twinFactoryService.findEntitySafe(entity.getTwinFactoryId()));
                if (entity.getInputTwinClass() == null || !entity.getInputTwinClass().getId().equals(entity.getInputTwinClassId()))
                    entity.setInputTwinClass(twinClassService.findEntitySafe(entity.getInputTwinClassId()));
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinFactoryEraserEntity createEraser(TwinFactoryEraserEntity createEntity) throws ServiceException {
        return saveSafe(createEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinFactoryEraserEntity updateEraser(TwinFactoryEraserEntity updateEntity) throws ServiceException {
        TwinFactoryEraserEntity dbEntity = findEntitySafe(updateEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinFactoryEraserEntity::getInputTwinClassId, TwinFactoryEraserEntity::setInputTwinClassId,
                TwinFactoryEraserEntity.Fields.inputTwinClassId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinFactoryEraserEntity::getTwinFactoryConditionSetId, TwinFactoryEraserEntity::setTwinFactoryConditionSetId,
                TwinFactoryEraserEntity.Fields.twinFactoryConditionSetId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinFactoryEraserEntity::getTwinFactoryConditionInvert, TwinFactoryEraserEntity::setTwinFactoryConditionInvert,
                TwinFactoryEraserEntity.Fields.twinFactoryConditionInvert, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinFactoryEraserEntity::getActive, TwinFactoryEraserEntity::setActive,
                TwinFactoryEraserEntity.Fields.active, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinFactoryEraserEntity::getDescription, TwinFactoryEraserEntity::setDescription,
                TwinFactoryEraserEntity.Fields.description, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinFactoryEraserEntity::getEraserAction, TwinFactoryEraserEntity::setEraserAction,
                TwinFactoryEraserEntity.Fields.eraserAction, changesHelper);
        return updateSafe(dbEntity, changesHelper);
    }

    @Transactional
    public void deleteEraser(UUID id) throws ServiceException {
        deleteSafe(id);
    }

}
