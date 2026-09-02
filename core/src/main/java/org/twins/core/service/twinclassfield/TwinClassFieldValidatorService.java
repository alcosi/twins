package org.twins.core.service.twinclassfield;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;
import org.twins.core.dao.validator.TwinClassFieldValidatorRepository;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldvalidator.FieldValidator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFieldValidatorService extends EntitySecureFindServiceImpl<TwinClassFieldValidatorEntity> {
    private final TwinClassFieldValidatorRepository repository;
    @Lazy
    private final FeaturerService featurerService;

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
    public boolean validateEntity(TwinClassFieldValidatorEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) {
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
}
