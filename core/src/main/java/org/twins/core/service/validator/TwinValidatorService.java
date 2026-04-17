package org.twins.core.service.validator;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.validator.ContainsTwinValidatorSet;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.validator.TwinValidatorCreate;
import org.twins.core.domain.validator.TwinValidatorUpdate;
import org.twins.core.featurer.twin.validator.TwinValidator;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinValidatorSetService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinValidatorService extends EntitySecureFindServiceImpl<TwinValidatorEntity> {
    private final TwinValidatorRepository twinValidatorRepository;
    private final AuthService authService;
    @Lazy
    private final FeaturerService featurerService;
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
        ApiUser apiUser = authService.getApiUser();
        twinValidatorSetService.loadTwinValidatorSet(entity);
        if (entity.getTwinValidatorSet().getDomainId() != null) {
            return !entity.getTwinValidatorSet().getDomainId().equals(apiUser.getDomainId());
        }
        return false;
    }

    @Override
    public boolean validateEntity(TwinValidatorEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinValidatorSetId() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " twinValidatorSetId is not specified");
        }
        if (entity.getTwinValidatorFeaturerId() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " twinValidatorFeaturerId is not specified");
        }
        return !isEntityReadDenied(entity, EntitySmartService.ReadPermissionCheckMode.none);
    }

    public <T extends ContainsTwinValidatorSet> void loadValidators(T entity) throws ServiceException {
        loadValidators(List.of(entity));
    }

    public <T extends ContainsTwinValidatorSet> void loadValidators(Collection<T> entities) throws ServiceException {
        Kit<T, UUID> needLoad = new Kit<>(T::getTwinValidatorSetId);
        for (T entity : entities) {
            if (entity.getTwinValidatorKit() == null && entity.getTwinValidatorSetId() != null) {
                needLoad.add(entity);
            }
        }

        if (needLoad.isEmpty()) {
            return;
        }

        KitGrouped<TwinValidatorEntity, UUID, UUID> validatorKit = new KitGrouped<>(
                twinValidatorRepository.findByTwinValidatorSetIdIn(needLoad.getIdSet()),
                TwinValidatorEntity::getId,
                TwinValidatorEntity::getTwinValidatorSetId);

        for (Map.Entry<UUID, T> entry : needLoad.getMap().entrySet()) {
            entry.getValue().setTwinValidatorKit(new Kit<>(validatorKit.getGrouped(entry.getKey()), TwinValidatorEntity::getId));
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinValidatorEntity> createTwinValidator(Collection<TwinValidatorCreate> validators) throws ServiceException {
        if (CollectionUtils.isEmpty(validators)) {
            return Collections.emptyList();
        }

        ApiUser apiUser = authService.getApiUser();
        List<TwinValidatorEntity> validatorsToSave = new ArrayList<>();

        for (TwinValidatorCreate validator : validators) {
            HashMap<String, String> validatorParams = validator.getValidatorParams() != null
                    ? new HashMap<>(validator.getValidatorParams())
                    : new HashMap<>();

            if (validator.getValidatorFeaturerId() != null) {
                featurerService.checkValid(validator.getValidatorFeaturerId(), validatorParams, TwinValidator.class);
                featurerService.prepareForStore(validator.getValidatorFeaturerId(), validatorParams);
            } else {
                throw new ServiceException(org.cambium.common.exception.ErrorCodeCommon.FEATURER_IS_NULL);
            }

            TwinValidatorEntity validatorEntity = new TwinValidatorEntity()
                    .setTwinValidatorSetId(validator.getTwinValidatorSetId())
                    .setTwinValidatorFeaturerId(validator.getValidatorFeaturerId())
                    .setTwinValidatorParams(validatorParams)
                    .setInvert(validator.getInvert() != null ? validator.getInvert() : false)
                    .setActive(validator.getActive() != null ? validator.getActive() : true)
                    .setDescription(validator.getDescription())
                    .setOrder(validator.getOrder())
                    .setCreatedByUserId(apiUser.getUserId())
                    .setCreatedAt(Timestamp.from(Instant.now()));

            validatorsToSave.add(validatorEntity);
        }

        return StreamSupport.stream(saveSafe(validatorsToSave).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinValidatorEntity> updateTwinValidator(Collection<TwinValidatorUpdate> validators) throws ServiceException {
        if (CollectionUtils.isEmpty(validators)) {
            return Collections.emptyList();
        }

        ChangesHelperMulti<TwinValidatorEntity> changes = new ChangesHelperMulti<>();
        List<TwinValidatorEntity> allEntities = new ArrayList<>(validators.size());

        for (TwinValidatorUpdate validator : validators) {
            TwinValidatorEntity entity = findEntitySafe(validator.getId());
            allEntities.add(entity);

            ChangesHelper changesHelper = new ChangesHelper();

            updateEntityFieldByValue(validator.getTwinValidatorSetId(), entity,
                    TwinValidatorEntity::getTwinValidatorSetId, TwinValidatorEntity::setTwinValidatorSetId,
                    TwinValidatorEntity.Fields.twinValidatorSetId, changesHelper);
            updateFieldValidatorFeaturerId(entity, validator.getValidatorFeaturerId(),
                    validator.getValidatorParams() != null ? new HashMap<>(validator.getValidatorParams()) : new HashMap<>(), changesHelper);
            updateEntityFieldByValue(validator.getInvert(), entity,
                    TwinValidatorEntity::isInvert, TwinValidatorEntity::setInvert,
                    TwinValidatorEntity.Fields.invert, changesHelper);
            updateEntityFieldByValue(validator.getActive(), entity,
                    TwinValidatorEntity::getActive, TwinValidatorEntity::setActive,
                    TwinValidatorEntity.Fields.active, changesHelper);
            updateEntityFieldByValue(validator.getDescription(), entity,
                    TwinValidatorEntity::getDescription, TwinValidatorEntity::setDescription,
                    TwinValidatorEntity.Fields.description, changesHelper);
            updateEntityFieldByValue(validator.getOrder(), entity,
                    TwinValidatorEntity::getOrder, TwinValidatorEntity::setOrder,
                    TwinValidatorEntity.Fields.order, changesHelper);

            changes.add(entity, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }

    public void updateFieldValidatorFeaturerId(TwinValidatorEntity dbTwinValidatorEntity, Integer newFeaturerId,
                                                HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        if (newFeaturerId == null || newFeaturerId == 0) {
            if (newFeaturerParams.isEmpty())
                return; // nothing was changed
            else
                newFeaturerId = dbTwinValidatorEntity.getTwinValidatorFeaturerId(); // only params were changed
        }
        if (changesHelper.isChanged(TwinValidatorEntity.Fields.twinValidatorFeaturerId,
                dbTwinValidatorEntity.getTwinValidatorFeaturerId(), newFeaturerId)) {
            featurerService.checkValid(newFeaturerId, newFeaturerParams, TwinValidator.class);
            dbTwinValidatorEntity.setTwinValidatorFeaturerId(newFeaturerId);
        }
        featurerService.prepareForStore(newFeaturerId, newFeaturerParams);
        if (!newFeaturerParams.equals(dbTwinValidatorEntity.getTwinValidatorParams())) {
            changesHelper.add(TwinValidatorEntity.Fields.twinValidatorParams,
                    dbTwinValidatorEntity.getTwinValidatorParams(), newFeaturerParams);
            dbTwinValidatorEntity.setTwinValidatorParams(newFeaturerParams);
        }
    }
}