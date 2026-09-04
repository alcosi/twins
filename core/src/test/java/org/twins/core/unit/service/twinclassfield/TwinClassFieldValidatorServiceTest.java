package org.twins.core.unit.service.twinclassfield;

import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;
import org.twins.core.dao.validator.TwinClassFieldValidatorRepository;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldvalidator.FieldValidator;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.twinclassfield.TwinClassFieldValidatorService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

class TwinClassFieldValidatorServiceTest extends BaseUnitTest {

    @Mock
    private TwinClassFieldValidatorRepository repository;
    @Mock
    private FeaturerService featurerService;
    @Mock
    private TwinClassFieldService twinClassFieldService;
    @Mock
    private I18nService i18nService;
    @Mock
    private FieldValidator fieldValidator;

    private TwinClassFieldValidatorService service;
    private TwinEntity twinEntity;
    private TwinClassFieldEntity twinClassFieldEntity;
    private FieldValue fieldValue;

    @BeforeEach
    void setUp() throws ServiceException {
        service = new TwinClassFieldValidatorService(repository, featurerService, twinClassFieldService, i18nService);
        twinEntity = new TwinEntity();
        twinClassFieldEntity = new TwinClassFieldEntity().setId(UUID.randomUUID());
        fieldValue = new FieldValueText(twinClassFieldEntity).setValue("some value");
        lenient().when(featurerService.getFeaturer(any(Integer.class), eq(FieldValidator.class))).thenReturn(fieldValidator);
        lenient().when(fieldValidator.isValid(any(TwinClassFieldValidatorEntity.class), any(TwinEntity.class), any(FieldValue.class), any())).thenReturn(ValidationResult.VALID);
    }

    @Test
    void kitNotLoaded_skipsValidation() throws Exception {
        var result = service.validateFieldValue(twinEntity, twinClassFieldEntity, fieldValue, Map.of());
        assertTrue(result.isValid());
    }

    @Test
    void emptyKit_isValid() throws Exception {
        twinClassFieldEntity.setFieldValidatorKit(Kit.emptyKit());
        var result = service.validateFieldValue(twinEntity, twinClassFieldEntity, fieldValue, Map.of());
        assertTrue(result.isValid());
    }

    @Test
    void allValidatorsPass_isValid() throws Exception {
        twinClassFieldEntity.setFieldValidatorKit(new Kit<>(List.of(validatorEntity()), TwinClassFieldValidatorEntity::getId));
        var result = service.validateFieldValue(twinEntity, twinClassFieldEntity, fieldValue, Map.of());
        assertTrue(result.isValid());
    }

    @Test
    void validatorFails_returnsItsResult() throws Exception {
        ValidationResult invalid = new ValidationResult(false, "value is incorrect");
        when(fieldValidator.isValid(any(TwinClassFieldValidatorEntity.class), any(TwinEntity.class), any(FieldValue.class), any())).thenReturn(invalid);
        twinClassFieldEntity.setFieldValidatorKit(new Kit<>(List.of(validatorEntity()), TwinClassFieldValidatorEntity::getId));
        var result = service.validateFieldValue(twinEntity, twinClassFieldEntity, fieldValue, Map.of());
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("incorrect"));
    }

    private TwinClassFieldValidatorEntity validatorEntity() {
        return new TwinClassFieldValidatorEntity()
                .setId(UUID.randomUUID())
                .setTwinClassFieldId(twinClassFieldEntity.getId())
                .setFieldValidatorFeaturerId(5601);
    }
}
