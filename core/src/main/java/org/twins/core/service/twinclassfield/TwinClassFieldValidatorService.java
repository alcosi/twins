package org.twins.core.service.twinclassfield;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;
import org.twins.core.dao.validator.TwinClassFieldValidatorRepository;
import org.twins.core.domain.twinclass.TwinClassFieldValidatorCreate;
import org.twins.core.domain.twinclass.TwinClassFieldValidatorUpdate;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldvalidator.FieldValidator;
import org.twins.core.service.i18n.I18nService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFieldValidatorService extends EntitySecureFindServiceImpl<TwinClassFieldValidatorEntity> {
    private final TwinClassFieldValidatorRepository repository;
    @Lazy
    private final FeaturerService featurerService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    private final I18nService i18nService;

    @Override
    public CrudRepository<TwinClassFieldValidatorEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinClassFieldValidatorEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldValidatorEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldValidatorEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFieldValidatorEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinClassFieldId() == null)
            return logErrorAndReturnFalse("twinClassFieldId is required for " + entity.logShort());
        if (entity.getFieldValidatorFeaturerId() == null)
            throw new ServiceException(ErrorCodeCommon.FEATURER_IS_NULL);
        return true;
    }

    public List<TwinClassFieldValidatorEntity> findByTwinClassFieldIdIn(Collection<UUID> twinClassFieldIds) {
        return repository.findByTwinClassFieldIdIn(twinClassFieldIds);
    }

    /**
     * Runs the backend field validators attached to the twin class field ({@code twin_class_field_validator} table).
     * Meant to be called after the field typer's own validation has passed.
     *
     * @param contextFields fields from the current create/update payload — payload values win over db values
     *                      during cross-field validation, see {@link FieldValidator#resolveFieldValue}
     */
    public ValidationResult validateFieldValue(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity, FieldValue fieldValue, Map<UUID, FieldValue> contextFields) throws ServiceException {
        Kit<TwinClassFieldValidatorEntity, UUID> fieldValidatorKit = twinClassFieldEntity.getFieldValidatorKit();
        if (fieldValidatorKit == null) // validators are not loaded for this field — skip silently
            return ValidationResult.VALID;
        for (TwinClassFieldValidatorEntity validatorEntity : fieldValidatorKit.getCollection()) {
            FieldValidator fieldValidator = featurerService.getFeaturer(validatorEntity.getFieldValidatorFeaturerId(), FieldValidator.class);
            ValidationResult validationResult = fieldValidator.isValid(validatorEntity, twinEntity, fieldValue, contextFields);
            if (!validationResult.isValid()) {
                log.error("{} value failed {}", twinClassFieldEntity.logNormal(), validatorEntity.logNormal());
                return validationResult;
            }
        }
        return ValidationResult.VALID;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassFieldValidatorEntity> createTwinClassFieldValidators(List<TwinClassFieldValidatorCreate> validators) throws ServiceException {
        if (validators == null || validators.isEmpty()) {
            return Collections.emptyList();
        }
        List<TwinClassFieldValidatorEntity> entitiesToSave = new ArrayList<>();
        for (TwinClassFieldValidatorCreate validator : validators) {
            TwinClassFieldValidatorEntity entity = validator.getTwinClassFieldValidator();
            twinClassFieldService.findEntitySafe(entity.getTwinClassFieldId());
            HashMap<String, String> validatorParams = entity.getFieldValidatorParams() != null
                    ? new HashMap<>(entity.getFieldValidatorParams())
                    : new HashMap<>();
            validateAndPrepareFeaturer(entity.getFieldValidatorFeaturerId(), validatorParams, FieldValidator.class);
            entity.setFieldValidatorParams(validatorParams);
            if (validator.getBeValidationErrorI18n() != null) {
                entity.setBeValidationErrorI18nId(
                        i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_FIELD_BE_VALIDATION_ERROR, validator.getBeValidationErrorI18n()).getId());
            }
            entitiesToSave.add(entity);
        }
        List<TwinClassFieldValidatorEntity> saved = StreamSupport.stream(saveSafe(entitiesToSave).spliterator(), false).toList();
        invalidateParentFieldValidatorKits(saved);
        return saved;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassFieldValidatorEntity> updateTwinClassFieldValidators(List<TwinClassFieldValidatorUpdate> validators) throws ServiceException {
        if (validators == null || validators.isEmpty()) {
            return Collections.emptyList();
        }
        ChangesHelperMulti<TwinClassFieldValidatorEntity> changes = new ChangesHelperMulti<>();
        Kit<TwinClassFieldValidatorEntity, UUID> entitiesKit = findEntitiesSafe(validators.stream().map(TwinClassFieldValidatorUpdate::getId).toList());
        List<TwinClassFieldValidatorEntity> allEntities = new ArrayList<>(validators.size());
        for (TwinClassFieldValidatorUpdate validator : validators) {
            TwinClassFieldValidatorEntity entity = entitiesKit.get(validator.getId());
            allEntities.add(entity);
            ChangesHelper changesHelper = new ChangesHelper();
            TwinClassFieldValidatorEntity sourceEntity = validator.getTwinClassFieldValidator();
            if (sourceEntity.getTwinClassFieldId() != null) {
                twinClassFieldService.findEntitySafe(sourceEntity.getTwinClassFieldId());
            }
            updateEntityFieldByValueIfNotNull(sourceEntity.getTwinClassFieldId(), entity,
                    TwinClassFieldValidatorEntity::getTwinClassFieldId, TwinClassFieldValidatorEntity::setTwinClassFieldId,
                    TwinClassFieldValidatorEntity.Fields.twinClassFieldId, changesHelper);
            updateEntityFeaturerField(entity, sourceEntity.getFieldValidatorFeaturerId(), sourceEntity.getFieldValidatorParams(),
                    TwinClassFieldValidatorEntity::getFieldValidatorFeaturerId, TwinClassFieldValidatorEntity::setFieldValidatorFeaturerId,
                    TwinClassFieldValidatorEntity::getFieldValidatorParams, TwinClassFieldValidatorEntity::setFieldValidatorParams,
                    TwinClassFieldValidatorEntity.Fields.fieldValidatorFeaturerId, TwinClassFieldValidatorEntity.Fields.fieldValidatorParams,
                    FieldValidator.class, changesHelper);
            i18nService.updateI18nFieldForEntity(validator.getBeValidationErrorI18n(), I18nType.TWIN_CLASS_FIELD_BE_VALIDATION_ERROR,
                    entity, TwinClassFieldValidatorEntity::getBeValidationErrorI18nId, TwinClassFieldValidatorEntity::setBeValidationErrorI18nId,
                    TwinClassFieldValidatorEntity.Fields.beValidationErrorI18nId, changesHelper);
            changes.add(entity, changesHelper);
        }
        updateSafe(changes);
        invalidateParentFieldValidatorKits(allEntities);
        return allEntities;
    }

    public void loadTwinClassField(TwinClassFieldValidatorEntity src) throws ServiceException {
        loadTwinClassField(Collections.singletonList(src));
    }

    public void loadTwinClassField(Collection<TwinClassFieldValidatorEntity> srcCollection) throws ServiceException {
        twinClassFieldService.load(
                srcCollection,
                TwinClassFieldValidatorEntity::getTwinClassFieldId,
                TwinClassFieldValidatorEntity::getTwinClassField,
                TwinClassFieldValidatorEntity::setTwinClassField
        );
    }

    private void invalidateParentFieldValidatorKits(Collection<TwinClassFieldValidatorEntity> validators) throws ServiceException {
        Set<UUID> twinClassFieldIds = new HashSet<>();
        for (TwinClassFieldValidatorEntity validator : validators) {
            if (validator.getTwinClassFieldId() != null)
                twinClassFieldIds.add(validator.getTwinClassFieldId());
        }
        if (twinClassFieldIds.isEmpty())
            return;
        for (TwinClassFieldEntity twinClassField : twinClassFieldService.findEntitiesSafe(twinClassFieldIds).getCollection()) {
            twinClassField.setFieldValidatorKit(null);
        }
    }
}
