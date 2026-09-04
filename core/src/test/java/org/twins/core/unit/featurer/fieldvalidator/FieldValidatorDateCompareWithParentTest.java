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
import org.twins.core.featurer.fieldvalidator.FieldValidatorDateCompareWithParent;
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
import static org.mockito.Mockito.when;

class FieldValidatorDateCompareWithParentTest extends BaseUnitTest {

    private static final String PATTERN = "yyyy-MM-dd";

    @Mock
    private FeaturerService featurerService;
    @Mock
    private TwinService twinService;
    @Mock
    private I18nService i18nService;

    private final UUID thisFieldId = UUID.randomUUID();
    private final UUID parentFieldId = UUID.randomUUID();

    private FieldValidatorDateCompareWithParent validator;
    private TwinEntity twinEntity;
    private TwinEntity headTwin;
    private TwinClassFieldValidatorEntity validatorEntity;

    @BeforeEach
    void setUp() throws Exception {
        validator = new FieldValidatorDateCompareWithParent();
        validator.featurerService = featurerService;
        setField(validator, "twinService", twinService);
        setField(validator, "i18nService", i18nService);
        lenient().when(featurerService.extractProperties(any(org.cambium.featurer.Featurer.class), any(HashMap.class))).thenAnswer(invocation -> {
            HashMap<String, String> params = invocation.getArgument(1);
            Properties properties = new Properties();
            properties.putAll(params);
            if (!properties.containsKey("compareOperator"))
                properties.put("compareOperator", "lt");
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
        headTwin = new TwinEntity();
        headTwin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
        when(twinService.loadHead(twinEntity)).thenReturn(headTwin);

        validatorEntity = new TwinClassFieldValidatorEntity()
                .setTwinClassFieldId(thisFieldId)
                .setFieldValidatorFeaturerId(5603)
                .setFieldValidatorParams(new HashMap<>(Map.of(
                        "parentTwinClassFieldId", parentFieldId.toString(),
                        "compareOperator", "lt")));
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

    private ValidationResult isValid(FieldValue value) throws ServiceException {
        return validator.isValid(validatorEntity, twinEntity, value, Map.of());
    }

    @Test
    void lt_passesWhenBeforeParent() throws Exception {
        headTwin.getFieldValuesKit().add(dateValue(parentFieldId, "2030-01-10"));
        assertTrue(isValid(dateValue("2030-01-01")).isValid());
    }

    @Test
    void lt_failsWhenEqualToParent() throws Exception {
        headTwin.getFieldValuesKit().add(dateValue(parentFieldId, "2030-01-10"));
        assertFalse(isValid(dateValue("2030-01-10")).isValid());
    }

    @Test
    void lt_failsWhenAfterParent() throws Exception {
        headTwin.getFieldValuesKit().add(dateValue(parentFieldId, "2030-01-10"));
        assertFalse(isValid(dateValue("2030-01-20")).isValid());
    }

    @Test
    void passesWhenNoHead() throws Exception {
        when(twinService.loadHead(twinEntity)).thenReturn(null);
        assertTrue(isValid(dateValue("2030-01-01")).isValid());
    }

    @Test
    void passesWhenParentFieldEmpty() throws Exception {
        assertTrue(isValid(dateValue("2030-01-01")).isValid());
    }

    @Test
    void le_passesOnEqualBoundary() throws Exception {
        validatorEntity.setFieldValidatorParams(new HashMap<>(Map.of(
                "parentTwinClassFieldId", parentFieldId.toString(),
                "compareOperator", "le")));
        headTwin.getFieldValuesKit().add(dateValue(parentFieldId, "2030-01-10"));
        assertTrue(isValid(dateValue("2030-01-10")).isValid());
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
