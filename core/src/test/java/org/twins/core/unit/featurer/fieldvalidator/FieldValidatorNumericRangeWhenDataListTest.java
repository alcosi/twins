package org.twins.core.unit.featurer.fieldvalidator;

import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldvalidator.FieldValidatorNumericRangeWhenDataList;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

class FieldValidatorNumericRangeWhenDataListTest extends BaseUnitTest {

    @Mock
    private FeaturerService featurerService;
    @Mock
    private TwinService twinService;

    private final UUID thisFieldId = UUID.randomUUID();
    private final UUID dataListFieldId = UUID.randomUUID();
    private final UUID percentOptionId = UUID.randomUUID();
    private final UUID otherOptionId = UUID.randomUUID();

    private FieldValidatorNumericRangeWhenDataList validator;
    private TwinEntity twinEntity;
    private TwinClassFieldValidatorEntity validatorEntity;

    @BeforeEach
    void setUp() throws Exception {
        validator = new FieldValidatorNumericRangeWhenDataList();
        validator.featurerService = featurerService;
        setField(validator, "twinService", twinService);
        lenient().when(featurerService.extractProperties(any(org.cambium.featurer.Featurer.class), any(HashMap.class))).thenAnswer(invocation -> {
            HashMap<String, String> params = invocation.getArgument(1);
            Properties properties = new Properties();
            properties.putAll(params);
            return properties;
        });
        lenient().doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.Collection<TwinEntity> twins = invocation.getArgument(0);
            for (TwinEntity twin : twins) {
                if (twin.getFieldValuesKit() == null)
                    twin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
            }
            return null;
        }).when(twinService).loadFieldsValues(anyCollection());
        lenient().doAnswer(invocation -> null).when(twinService).loadHead(anyCollection());

        twinEntity = new TwinEntity();
        twinEntity.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));

        validatorEntity = new TwinClassFieldValidatorEntity()
                .setTwinClassFieldId(thisFieldId)
                .setFieldValidatorFeaturerId(5604)
                .setFieldValidatorParams(new HashMap<>(Map.of(
                        "dataListTwinClassFieldId", dataListFieldId.toString(),
                        "dataListOptionId", percentOptionId.toString(),
                        "min", "0",
                        "max", "100")));
    }

    private TwinClassFieldEntity twinClassField(UUID id) {
        return new TwinClassFieldEntity().setId(id);
    }

    private FieldValueText numericValue(String value) {
        return new FieldValueText(twinClassField(thisFieldId)).setValue(value);
    }

    private FieldValueSelect dataListValue(UUID optionId) {
        DataListOptionEntity option = new DataListOptionEntity().setId(optionId);
        return (FieldValueSelect) new FieldValueSelect(twinClassField(dataListFieldId)).add(option);
    }

    private ValidationResult isValid(FieldValue value, Map<UUID, FieldValue> contextFields) throws ServiceException {
        return validator.isValid(validatorEntity, twinEntity, value, contextFields);
    }

    @Test
    void percentUnit_outOfRange_fails() throws Exception {
        var result = isValid(numericValue("150"), Map.of(dataListFieldId, dataListValue(percentOptionId)));
        assertFalse(result.isValid());
        verify(twinService).loadFieldsValues(anyCollection());
    }

    @Test
    void percentUnit_inRange_passes() throws Exception {
        var result = isValid(numericValue("50"), Map.of(dataListFieldId, dataListValue(percentOptionId)));
        assertTrue(result.isValid());
    }

    @Test
    void percentUnit_boundaryValues_pass() throws Exception {
        assertTrue(isValid(numericValue("0"), Map.of(dataListFieldId, dataListValue(percentOptionId))).isValid());
        assertTrue(isValid(numericValue("100"), Map.of(dataListFieldId, dataListValue(percentOptionId))).isValid());
    }

    @Test
    void otherUnit_skipsRangeCheck() throws Exception {
        var result = isValid(numericValue("150"), Map.of(dataListFieldId, dataListValue(otherOptionId)));
        assertTrue(result.isValid());
    }

    @Test
    void missingUnit_skipsRangeCheck() throws Exception {
        var result = isValid(numericValue("150"), Map.of());
        assertTrue(result.isValid());
    }

    @Test
    void emptyValue_skipsValidation() throws Exception {
        FieldValueText empty = new FieldValueText(twinClassField(thisFieldId));
        var result = isValid(empty, Map.of(dataListFieldId, dataListValue(percentOptionId)));
        assertTrue(result.isValid());
    }

    @Test
    void onParent_usesHeadDataList() throws Exception {
        validatorEntity.setFieldValidatorParams(new HashMap<>(Map.of(
                "dataListTwinClassFieldId", dataListFieldId.toString(),
                "dataListOptionId", percentOptionId.toString(),
                "min", "0",
                "max", "100",
                "onParent", "true")));
        TwinEntity head = new TwinEntity();
        head.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
        head.getFieldValuesKit().add(dataListValue(percentOptionId));
        twinEntity.setHeadTwin(head);

        var result = isValid(numericValue("150"), Map.of());
        assertFalse(result.isValid());
        verify(twinService).loadHead(anyCollection());
    }

    @Test
    void nonNumericValue_fails() throws Exception {
        var result = isValid(numericValue("abc"), Map.of(dataListFieldId, dataListValue(percentOptionId)));
        assertFalse(result.isValid());
    }

    @Test
    void nullBounds_skipRangeCheck() throws Exception {
        validatorEntity.setFieldValidatorParams(new HashMap<>(Map.of(
                "dataListTwinClassFieldId", dataListFieldId.toString(),
                "dataListOptionId", percentOptionId.toString())));
        var result = isValid(numericValue("150"), Map.of(dataListFieldId, dataListValue(percentOptionId)));
        assertTrue(result.isValid());
    }

    @Test
    void onlyMaxBound_checksUpperLimit() throws Exception {
        validatorEntity.setFieldValidatorParams(new HashMap<>(Map.of(
                "dataListTwinClassFieldId", dataListFieldId.toString(),
                "dataListOptionId", percentOptionId.toString(),
                "max", "100")));
        assertTrue(isValid(numericValue("-5"), Map.of(dataListFieldId, dataListValue(percentOptionId))).isValid());
        assertFalse(isValid(numericValue("150"), Map.of(dataListFieldId, dataListValue(percentOptionId))).isValid());
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
