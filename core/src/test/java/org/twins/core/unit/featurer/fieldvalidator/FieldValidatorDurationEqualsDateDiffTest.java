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
import org.twins.core.featurer.fieldvalidator.FieldValidatorDurationEqualsDateDiff;
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
import static org.mockito.Mockito.lenient;

class FieldValidatorDurationEqualsDateDiffTest extends BaseUnitTest {

    private static final String PATTERN = "yyyy-MM-dd";

    @Mock
    private FeaturerService featurerService;
    @Mock
    private TwinService twinService;
    @Mock
    private I18nService i18nService;

    private final UUID durationFieldId = UUID.randomUUID();
    private final UUID startFieldId = UUID.randomUUID();
    private final UUID endFieldId = UUID.randomUUID();

    private FieldValidatorDurationEqualsDateDiff validator;
    private TwinEntity twinEntity;
    private TwinClassFieldValidatorEntity validatorEntity;

    @BeforeEach
    void setUp() throws Exception {
        validator = new FieldValidatorDurationEqualsDateDiff();
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
        lenient().doAnswer(invocation -> {
            TwinEntity twin = invocation.getArgument(0);
            if (twin.getFieldValuesKit() == null)
                twin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
            return null;
        }).when(twinService).loadFieldsValues(any(TwinEntity.class));

        twinEntity = new TwinEntity();
        twinEntity.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));

        validatorEntity = new TwinClassFieldValidatorEntity()
                .setTwinClassFieldId(durationFieldId)
                .setFieldValidatorFeaturerId(5602)
                .setFieldValidatorParams(new HashMap<>(Map.of(
                        "startDateTwinClassFieldId", startFieldId.toString(),
                        "endDateTwinClassFieldId", endFieldId.toString())));
    }

    private FieldValueText durationValue(String days) {
        return new FieldValueText(twinClassField(durationFieldId)).setValue(days);
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
    void passesWhenDurationMatchesDayDiff() throws Exception {
        var result = isValid(durationValue("9"), Map.of(
                startFieldId, dateValue(startFieldId, "2030-01-01"),
                endFieldId, dateValue(endFieldId, "2030-01-10")));
        assertTrue(result.isValid());
    }

    @Test
    void failsWhenDurationDoesNotMatch() throws Exception {
        var result = isValid(durationValue("5"), Map.of(
                startFieldId, dateValue(startFieldId, "2030-01-01"),
                endFieldId, dateValue(endFieldId, "2030-01-10")));
        assertFalse(result.isValid());
    }

    @Test
    void passesWhenStartMissing() throws Exception {
        var result = isValid(durationValue("5"), Map.of(
                endFieldId, dateValue(endFieldId, "2030-01-10")));
        assertTrue(result.isValid());
    }

    @Test
    void passesWhenEndMissing() throws Exception {
        var result = isValid(durationValue("5"), Map.of(
                startFieldId, dateValue(startFieldId, "2030-01-01")));
        assertTrue(result.isValid());
    }

    @Test
    void passesOnEqualDatesWithZeroDuration() throws Exception {
        var result = isValid(durationValue("0"), Map.of(
                startFieldId, dateValue(startFieldId, "2030-01-10"),
                endFieldId, dateValue(endFieldId, "2030-01-10")));
        assertTrue(result.isValid());
    }

    @Test
    void emptyDuration_skipsValidation() throws Exception {
        FieldValueText empty = new FieldValueText(twinClassField(durationFieldId));
        var result = isValid(empty, Map.of(
                startFieldId, dateValue(startFieldId, "2030-01-01"),
                endFieldId, dateValue(endFieldId, "2030-01-10")));
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
