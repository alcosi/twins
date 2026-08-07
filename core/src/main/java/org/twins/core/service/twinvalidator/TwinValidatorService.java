package org.twins.core.service.twinvalidator;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.validator.ContainsTwinValidatorSet;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorRepository;
import org.twins.core.domain.validator.TwinValidatorCreate;
import org.twins.core.domain.validator.TwinValidatorUpdate;
import org.twins.core.featurer.twin.validator.TwinValidator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinValidatorService extends EntitySecureFindServiceImpl<TwinValidatorEntity> {
    private final TwinValidatorRepository twinValidatorRepository;
    @Lazy
    private final TwinValidatorSetService twinValidatorSetService;

    @Override
    public CrudRepository<TwinValidatorEntity, UUID> entityRepository() {
        return twinValidatorRepository;
    }

    @Override
    public Function<TwinValidatorEntity, UUID> entityGetIdFunction() {
        return TwinValidatorEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinValidatorEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinValidatorEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinValidatorEntity> createTwinValidators(List<TwinValidatorCreate> validators) throws ServiceException {
        if (validators == null || validators.isEmpty()) {
            return Collections.emptyList();
        }
        List<TwinValidatorEntity> entitiesToSave = new ArrayList<>();
        for (TwinValidatorCreate validator : validators) {
            TwinValidatorEntity entity = validator.getTwinValidator();
            // prepareForStore mutates params in place — work on a copy so the DTO/domain object is untouched
            HashMap<String, String> validatorParams = entity.getTwinValidatorParams() != null
                    ? new HashMap<>(entity.getTwinValidatorParams())
                    : new HashMap<>();
            if (entity.getTwinValidatorFeaturerId() != null) {
                validateAndPrepareFeaturer(entity.getTwinValidatorFeaturerId(), validatorParams, TwinValidator.class);
            } else {
                throw new ServiceException(ErrorCodeCommon.FEATURER_IS_NULL);
            }
            entity
                    .setTwinValidatorParams(validatorParams)
                    .setInvert(entity.getInvert() != null ? entity.getInvert() : false)
                    .setActive(entity.getActive() != null ? entity.getActive() : true);
            entitiesToSave.add(entity);
        }
        return StreamSupport.stream(saveSafe(entitiesToSave).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinValidatorEntity> updateTwinValidators(List<TwinValidatorUpdate> validators) throws ServiceException {
        if (validators == null || validators.isEmpty()) {
            return Collections.emptyList();
        }
        ChangesHelperMulti<TwinValidatorEntity> changes = new ChangesHelperMulti<>();
        Kit<TwinValidatorEntity, UUID> entitiesKit = findEntitiesSafe(validators.stream().map(TwinValidatorUpdate::getId).toList());
        List<TwinValidatorEntity> allEntities = new ArrayList<>(validators.size());
        for (TwinValidatorUpdate validator : validators) {
            TwinValidatorEntity entity = entitiesKit.get(validator.getId());
            allEntities.add(entity);
            ChangesHelper changesHelper = new ChangesHelper();
            TwinValidatorEntity sourceEntity = validator.getTwinValidator();
            updateEntityFieldByValueIfNotNull(sourceEntity.getTwinValidatorSetId(), entity, TwinValidatorEntity::getTwinValidatorSetId, TwinValidatorEntity::setTwinValidatorSetId, TwinValidatorEntity.Fields.twinValidatorSetId, changesHelper);
            updateEntityFeaturerField(entity, sourceEntity.getTwinValidatorFeaturerId(), sourceEntity.getTwinValidatorParams(),
                    TwinValidatorEntity::getTwinValidatorFeaturerId, TwinValidatorEntity::setTwinValidatorFeaturerId,
                    TwinValidatorEntity::getTwinValidatorParams, TwinValidatorEntity::setTwinValidatorParams,
                    TwinValidatorEntity.Fields.twinValidatorFeaturerId, TwinValidatorEntity.Fields.twinValidatorParams,
                    TwinValidator.class, changesHelper);
            updateEntityFieldByValueIfNotNull(sourceEntity.getInvert(), entity, TwinValidatorEntity::getInvert, TwinValidatorEntity::setInvert, TwinValidatorEntity.Fields.invert, changesHelper);
            updateEntityFieldByValueIfNotNull(sourceEntity.getActive(), entity, TwinValidatorEntity::getActive, TwinValidatorEntity::setActive, TwinValidatorEntity.Fields.active, changesHelper);
            updateEntityFieldByValueIfNotNull(sourceEntity.getDescription(), entity, TwinValidatorEntity::getDescription, TwinValidatorEntity::setDescription, TwinValidatorEntity.Fields.description, changesHelper);
            updateEntityFieldByValueIfNotNull(sourceEntity.getOrder(), entity, TwinValidatorEntity::getOrder, TwinValidatorEntity::setOrder, TwinValidatorEntity.Fields.order, changesHelper);
            changes.add(entity, changesHelper);
        }
        updateSafe(changes);
        return allEntities;
    }

    public <T extends ContainsTwinValidatorSet> void loadValidators(T entity) throws ServiceException {
        loadValidators(List.of(entity));
    }

    public <T extends ContainsTwinValidatorSet> void loadValidators(Collection<T> entities) throws ServiceException {
        loadKit(entities,
                ContainsTwinValidatorSet::getTwinValidatorSetId,
                ContainsTwinValidatorSet::getTwinValidatorKit,
                ContainsTwinValidatorSet::setTwinValidatorKit,
                twinValidatorRepository::findByTwinValidatorSetIdIn,
                TwinValidatorEntity::getId,
                TwinValidatorEntity::getTwinValidatorSetId,
                (child, parent) -> {});
    }

    public void loadTwinValidatorSet(TwinValidatorEntity src) throws ServiceException {
        twinValidatorSetService.loadTwinValidatorSet(src);
    }

    public void loadTwinValidatorSet(Collection<TwinValidatorEntity> srcCollection) throws ServiceException {
        twinValidatorSetService.loadTwinValidatorSet(srcCollection);
    }
}