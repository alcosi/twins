package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.ConditionerContextValueExists;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerContextValueExistsTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromContextFields lookuper;

    private ConditionerContextValueExists conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerContextValueExists();
        setField(conditioner, "fieldLookupers", fieldLookupers);
        when(fieldLookupers.getFromContextFields()).thenReturn(lookuper);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
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

    private Properties buildProperties(UUID fieldId) {
        var props = new Properties();
        props.put("twinClassFieldId", fieldId.toString());
        return props;
    }

    @Nested
    class Check {

        @Test
        void check_valuePresent_returnsTrue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var props = buildProperties(fieldId);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), eq(fieldId)))
                    .thenReturn(mock(FieldValue.class));

            assertTrue(conditioner.check(props, mock(FactoryItem.class)));
        }

        @Test
        void check_valueNull_returnsFalse() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var props = buildProperties(fieldId);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), eq(fieldId))).thenReturn(null);

            assertFalse(conditioner.check(props, mock(FactoryItem.class)));
        }

        @Test
        void check_lookupThrows_returnsFalse() throws ServiceException {
            // contract: existence check must swallow lookup failures and report "does not exist"
            var fieldId = UUID.randomUUID();
            var props = buildProperties(fieldId);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), eq(fieldId)))
                    .thenThrow(new ServiceException(org.twins.core.exception.ErrorCodeTwins.UUID_IS_NULL));

            assertFalse(conditioner.check(props, mock(FactoryItem.class)));
        }
    }
}
