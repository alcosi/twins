package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.UUID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import org.twins.core.domain.factory.FactoryEraserDuplicate;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class FactoryEraserService extends EntitySecureFindServiceImpl<TwinFactoryEraserEntity> {
    @Getter
    private final TwinFactoryEraserRepository repository;
    private final AuthService authService;
    @Lazy
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
        return checkDomainAccessDenied(entity.getTwinFactory().getDomainId(), entity.logNormal(), readPermissionCheckMode);
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

    public void duplicateErasersForFactory(TwinFactoryEntity fromFactory, TwinFactoryEntity toFactory) throws ServiceException {
        List<TwinFactoryEraserEntity> erasers = fromFactory.getTwinFactoryEraserKit().getList();
        if (CollectionUtils.isEmpty(erasers)) {
            return;
        }
        var entitiesForSave = new ArrayList<TwinFactoryEraserEntity>();
        for (TwinFactoryEraserEntity originalEraser : erasers) {
            TwinFactoryEraserEntity duplicateEraser = new TwinFactoryEraserEntity()
                    .setTwinFactoryId(toFactory.getId())
                    .setInputTwinClassId(originalEraser.getInputTwinClassId())
                    .setTwinFactoryConditionSetId(originalEraser.getTwinFactoryConditionSetId())
                    .setTwinFactoryConditionInvert(originalEraser.getTwinFactoryConditionInvert())
                    .setEraserAction(originalEraser.getEraserAction())
                    .setDescription(originalEraser.getDescription())
                    .setActive(originalEraser.getActive());
            entitiesForSave.add(duplicateEraser);
        }
        saveSafe(entitiesForSave);
    }

    @Transactional
    public Collection<TwinFactoryEraserEntity> duplicateErasers(Collection<FactoryEraserDuplicate> duplicates) throws ServiceException {
        if (CollectionUtils.isEmpty(duplicates)) {
            return Collections.emptyList();
        }
        loadOriginalErasers(duplicates);
        for (var duplicate : duplicates) {
            if (duplicate.getNewTwinFactoryId() == null) {
                duplicate.setNewTwinFactoryId(duplicate.getOriginalFactoryEraser().getTwinFactoryId());
            }
        }
        var entitiesForSave = new ArrayList<TwinFactoryEraserEntity>();
        for (var duplicate : duplicates) {
            TwinFactoryEraserEntity duplicateEraser = duplicateEraserEntity(duplicate.getOriginalFactoryEraser(), duplicate.getNewTwinFactoryId());
            entitiesForSave.add(duplicateEraser);
        }
        return StreamSupport.stream(saveSafe(entitiesForSave).spliterator(), false).toList();
    }

    private void loadOriginalErasers(Collection<FactoryEraserDuplicate> duplicates) throws ServiceException {
        load(duplicates,
                FactoryEraserDuplicate::getNewFactoryEraserId,
                FactoryEraserDuplicate::getOriginalFactoryEraserId,
                FactoryEraserDuplicate::getOriginalFactoryEraser,
                FactoryEraserDuplicate::setOriginalFactoryEraser);
    }

    private TwinFactoryEraserEntity duplicateEraserEntity(TwinFactoryEraserEntity srcEraserEntity, UUID newTwinFactoryId) throws ServiceException {
        return new TwinFactoryEraserEntity()
                .setTwinFactoryId(newTwinFactoryId)
                .setInputTwinClassId(srcEraserEntity.getInputTwinClassId())
                .setTwinFactoryConditionSetId(srcEraserEntity.getTwinFactoryConditionSetId())
                .setTwinFactoryConditionInvert(srcEraserEntity.getTwinFactoryConditionInvert())
                .setEraserAction(srcEraserEntity.getEraserAction())
                .setDescription(srcEraserEntity.getDescription())
                .setActive(srcEraserEntity.getActive());
    }

}
