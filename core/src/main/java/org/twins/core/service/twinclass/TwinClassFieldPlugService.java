package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldPlugEntity;
import org.twins.core.dao.twinclass.TwinClassFieldPlugRepository;
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

                if (twinClassFieldPlugRepository.existsByTwinClassIdAndTwinClassFieldId(entity.getTwinClassId(), entity.getTwinClassFieldId())) {
                    return logErrorAndReturnFalse(ErrorCodeTwins.ENTITY_ALREADY_EXIST.getMessage());
                }

                if (!twinClassService.existsById(entity.getTwinClassId())) {
                    return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN.getMessage());
                }

                if (!twinClassFieldService.existsById(entity.getTwinClassFieldId())) {
                    return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_FIELD_ID_UNKNOWN.getMessage());
                }

                TwinClassFieldEntity field = twinClassFieldService.findEntitySafe(entity.getTwinClassFieldId());
                if (field == null) {
                    return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_FIELD_ID_UNKNOWN.getMessage());
                } else if (field.getTwinClassFieldVisibilityId() != TwinClassFieldEntity.TwinClassFieldVisibility.PLUGGABLE) {
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
        if (entities == null || entities.isEmpty()) {
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
}
