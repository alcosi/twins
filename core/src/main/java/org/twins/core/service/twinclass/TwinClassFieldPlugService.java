package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldPlugEntity;
import org.twins.core.dao.twinclass.TwinClassFieldPlugRepository;
import org.twins.core.enums.twinclass.TwinClassFieldVisibility;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldPlugService extends EntitySecureFindServiceImpl<TwinClassFieldPlugEntity> {

    private final TwinClassFieldPlugRepository twinClassFieldPlugRepository;
    private final TwinClassService twinClassService;
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public CrudRepository<TwinClassFieldPlugEntity, UUID> entityRepository() {
        return twinClassFieldPlugRepository;
    }

    @Override
    public Function<TwinClassFieldPlugEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldPlugEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldPlugEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFieldPlugEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        switch (entityValidateMode) {
            case beforeSave -> {
                if (entity.getTwinClassId() == null || entity.getTwinClassFieldId() == null) {
                    return logErrorAndReturnFalse(ErrorCodeTwins.ENTITY_INVALID.getMessage());
                }

                if (existsByTwinClassIdAndTwinClassFieldId(entity.getTwinClassId(), entity.getTwinClassFieldId())) {
                    return logErrorAndReturnFalse(ErrorCodeTwins.ENTITY_ALREADY_EXIST.getMessage());
                }

                TwinClassFieldEntity field = twinClassFieldService.findEntitySafe(entity.getTwinClassFieldId());
                if (field == null) {
                    return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_FIELD_ID_UNKNOWN.getMessage());
                } else if (field.getTwinClassFieldVisibilityId() != TwinClassFieldVisibility.PLUGGABLE) {
                    return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_FIELD_IS_NOT_PLUGGABLE.getMessage());
                }

                if (entity.getTwinClass() == null || !entity.getTwinClass().getId().equals(entity.getTwinClassId())) {
                    try {
                        entity.setTwinClass(twinClassService.findEntitySafe(entity.getTwinClassId()));
                    } catch (ServiceException e) {
                        return logErrorAndReturnFalse("TwinClass with id[" + entity.getTwinClassId() + "] does not exist");
                    }
                }

                if (entity.getTwinClassField() == null || !entity.getTwinClassField().getId().equals(entity.getTwinClassFieldId())) {
                    try {
                        entity.setTwinClassField(twinClassFieldService.findEntitySafe(entity.getTwinClassId()));
                    } catch (ServiceException e) {
                        return logErrorAndReturnFalse("TwinClassField with id[" + entity.getTwinClassFieldId() + "] does not exist");
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean validateEntities(Collection<TwinClassFieldPlugEntity> entities, EntitySmartService.EntityValidateMode entityValidateMode) {
        try {
            twinClassService.findEntitiesSafe(entities.stream().map(TwinClassFieldPlugEntity::getTwinClassId).toList());
        } catch (ServiceException e) {
            return logErrorAndReturnFalse("List contains invalid class ids");
        }

        try {
            twinClassFieldService.findEntitiesSafe(entities.stream().map(TwinClassFieldPlugEntity::getTwinClassFieldId).toList());
        } catch (ServiceException e) {
            return logErrorAndReturnFalse("List contains invalid field ids");
        }

        if (!existsNone(entities)) {
            return logErrorAndReturnFalse("Entity from list already exists in db");
        }

        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinClassFieldPlugEntity plugField(TwinClassFieldPlugEntity entity) throws ServiceException {
        return plugFields(List.of(entity)).getFirst();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassFieldPlugEntity> plugFields(Collection<TwinClassFieldPlugEntity> entities) throws ServiceException {
        return StreamSupport.stream(saveSafe(entities).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public void unplugField(TwinClassFieldPlugEntity entity) throws ServiceException {
        unplugFields(List.of(entity));
    }

    @Transactional(rollbackFor = Throwable.class)
    public void unplugFields(Collection<TwinClassFieldPlugEntity> entities) throws ServiceException {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }

        List<TwinClassFieldPlugEntity> entitiesToDelete = new ArrayList<>();

        for (var entity : entities) {
            TwinClassFieldPlugEntity entityToDelete = twinClassFieldPlugRepository.findByTwinClassIdAndTwinClassFieldId(entity.getTwinClassId(), entity.getTwinClassFieldId());

            if (entityToDelete != null) {
                entitiesToDelete.add(entityToDelete);
            } else {
                log.info("There is no entity {}", entity.logNormal());
                throw new ServiceException(ErrorCodeTwins.ENTITY_INVALID, getValidationErrorMessage(entity));
            }
        }

        entitySmartService.deleteAllEntitiesAndLog(entitiesToDelete, entityRepository());
    }

    public void loadClasses(Collection<TwinClassFieldPlugEntity> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }

        List<TwinClassFieldPlugEntity> needLoad = new ArrayList<>();
        for (var entity : entities) {
            if (entity.getTwinClass() == null) {
                needLoad.add(entity);
            }
        }

        if (needLoad.isEmpty()) {
            return;
        }

        KitGrouped<TwinClassFieldPlugEntity, UUID, UUID> entitiesKit = new KitGrouped<>(needLoad, TwinClassFieldPlugEntity::getId, TwinClassFieldPlugEntity::getTwinClassId);
        Kit<TwinClassEntity, UUID> classesKit = new Kit<>(twinClassService.findAllByIdIn(entitiesKit.getGroupedKeySet()), TwinClassEntity::getId);

        for (var entry : classesKit.getMap().entrySet()) {
            for (var entity : entitiesKit.getGrouped(entry.getKey())) {
                entity.setTwinClass(entry.getValue());
            }
        }
    }

    public void loadFields(Collection<TwinClassFieldPlugEntity> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }

        List<TwinClassFieldPlugEntity> needLoad = new ArrayList<>();
        for (var entity : entities) {
            if (entity.getTwinClassField() == null) {
                needLoad.add(entity);
            }
        }

        if (needLoad.isEmpty()) {
            return;
        }

        KitGrouped<TwinClassFieldPlugEntity, UUID, UUID> entitiesKit = new KitGrouped<>(needLoad, TwinClassFieldPlugEntity::getId, TwinClassFieldPlugEntity::getTwinClassFieldId);
        Kit<TwinClassFieldEntity, UUID> classesKit = new Kit<>(twinClassFieldService.findAllByIdIn(entitiesKit.getGroupedKeySet()), TwinClassFieldEntity::getId);

        for (var entry : classesKit.getMap().entrySet()) {
            for (var entity : entitiesKit.getGrouped(entry.getKey())) {
                entity.setTwinClassField(entry.getValue());
            }
        }
    }

    public boolean existsByTwinClassIdAndTwinClassFieldId(UUID twinClassId, UUID twinClassFieldId) {
        return twinClassFieldPlugRepository.existsByTwinClassIdAndTwinClassFieldId(twinClassId, twinClassFieldId);
    }

    public boolean existsNone(Collection<TwinClassFieldPlugEntity> entities) {
        return entities.stream()
                .noneMatch(entity -> existsByTwinClassIdAndTwinClassFieldId(entity.getTwinClassId(), entity.getTwinClassFieldId()));
    }
}
