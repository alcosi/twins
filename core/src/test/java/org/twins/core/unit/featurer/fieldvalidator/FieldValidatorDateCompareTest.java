package org.twins.core.unit.featurer.fieldvalidator;

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
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldvalidator.FieldValidatorDateCompare;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

class FieldValidatorDateCompareTest extends BaseUnitTest {

    private static final String PATTERN = "yyyy-MM-dd";

    @Mock
    private FeaturerService featurerService;
    @Mock
    private TwinService twinService;
    @Mock
    private I18nService i18nService;

    private final UUID thisFieldId = UUID.randomUUID();
    private final UUID otherFieldId = UUID.randomUUID();

    private FieldValidatorDateCompare validator;
    private TwinEntity twinEntity;
    private TwinClassFieldValidatorEntity validatorEntity;

    @BeforeEach
    void setUp() throws Exception {
        validator = new FieldValidatorDateCompare();
        validator.featurerService = featurerService;
        setField(validator, "twinService", twinService);
        setField(validator, "i18nService", i18nService);
        lenient().when(featurerService.extractProperties(any(org.cambium.featurer.Featurer.class), any(HashMap.class))).thenAnswer(invocation -> {
            HashMap<String, String> params = invocation.getArgument(1);
            Properties properties = new Properties();
            properties.putAll(params);
            return properties;
        });
        lenient().when(twinService.getErrorMessage(any(), any())).thenReturn("value is incorrect");
        // idempotent db load: just makes sure the kit is present
        lenient().doAnswer(invocation -> {
            TwinEntity twin = invocation.getArgument(0);
            if (twin.getFieldValuesKit() == null)
                twin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
            return null;
        }).when(twinService).loadFieldsValues(any(TwinEntity.class));

        twinEntity = new TwinEntity();
        twinEntity.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));

        validatorEntity = new TwinClassFieldValidatorEntity()
                .setTwinClassFieldId(thisFieldId)
                .setFieldValidatorFeaturerId(5601)
                .setFieldValidatorParams(new HashMap<>(Map.of(
                        "twinClassFieldIdToCompare", otherFieldId.toString(),
                        "compareOperator", "ge")));
    }

    private FieldValueDate dateValue(String dateStr) throws ServiceException {
        return dateValue(thisFieldId, dateStr);
    }

    private FieldValueDate dateValue(UUID fieldId, String dateStr) throws ServiceException {
        return (FieldValueDate) new FieldValueDate(twinClassField(fieldId), PATTERN).setDate(dateStr);
    }

    private TwinClassFieldEntity twinClassField(UUID id) {
        return new TwinClassFieldEntity().setId(id);
    }

    private ValidationResult isValid(FieldValue value, Map<UUID, FieldValue> contextFields) throws ServiceException {
        return validator.isValid(validatorEntity, twinEntity, value, contextFields);
    }

    @Test
    void ge_compareWithPayloadValue_failsWhenBefore() throws Exception {
        var result = isValid(dateValue("2030-01-01"), Map.of(otherFieldId, dateValue(otherFieldId, "2030-01-10")));
        assertFalse(result.isValid());
    }

    @Test
    void ge_compareWithPayloadValue_passesOnEqualBoundary() throws Exception {
        var result = isValid(dateValue("2030-01-10"), Map.of(otherFieldId, dateValue(otherFieldId, "2030-01-10")));
        assertTrue(result.isValid());
    }

    @Test
    void ge_compareWithDbValueWhenNotInPayload() throws Exception {
        twinEntity.getFieldValuesKit().add(dateValue(otherFieldId, "2030-01-05"));
        var result = isValid(dateValue("2030-01-01"), Map.of());
        assertFalse(result.isValid());
    }

    @Test
    void ge_otherFieldMissingEverywhere_passes() throws Exception {
        var result = isValid(dateValue("2030-01-01"), Map.of());
        assertTrue(result.isValid());
    }

    @Test
    void ge_otherFieldExplicitlyEmptyInPayload_passes() throws Exception {
        FieldValueDate emptyOther = new FieldValueDate(twinClassField(otherFieldId), PATTERN); // undefined
        var result = isValid(dateValue("2030-01-01"), Map.of(otherFieldId, emptyOther));
        assertTrue(result.isValid());
    }

    @Test
    void le_operator_passesWhenBefore() throws Exception {
        validatorEntity.setFieldValidatorParams(new HashMap<>(Map.of(
                "twinClassFieldIdToCompare", otherFieldId.toString(),
                "compareOperator", "le")));
        var result = isValid(dateValue("2030-01-01"), Map.of(otherFieldId, dateValue(otherFieldId, "2030-01-10")));
        assertTrue(result.isValid());
    }

    @Test
    void operatorDefaultsToGe() throws Exception {
        validatorEntity.setFieldValidatorParams(new HashMap<>(Map.of(
                "twinClassFieldIdToCompare", otherFieldId.toString())));
        var result = isValid(dateValue("2030-01-01"), Map.of(otherFieldId, dateValue(otherFieldId, "2030-01-10")));
        assertFalse(result.isValid());
    }

    @Test
    void nonDateValue_failsAsMisconfiguration() throws Exception {
        FieldValueText textValue = new FieldValueText(twinClassField(thisFieldId)).setValue("some text");
        var result = isValid(textValue, Map.of(otherFieldId, dateValue(otherFieldId, "2030-01-10")));
        assertFalse(result.isValid());
    }

    @Test
    void emptyValue_skipsValidation() throws Exception {
        FieldValueDate emptyValue = new FieldValueDate(twinClassField(thisFieldId), PATTERN); // undefined
        var result = isValid(emptyValue, Map.of(otherFieldId, dateValue(otherFieldId, "2030-01-10")));
        assertTrue(result.isValid());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }
}
