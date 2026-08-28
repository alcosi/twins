package org.twins.core.unit.featurer.fieldinitializer;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.featurer.fieldinitializer.FieldInitializerList;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldInitializerListTest extends BaseUnitTest {

    @Mock
    private DataListOptionService dataListOptionService;

    private FieldInitializerList initializer;

    @BeforeEach
    void setUp() throws Exception {
        initializer = new FieldInitializerList();
        setField(initializer, "dataListOptionService", dataListOptionService);
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

    private TwinClassFieldEntity buildTwinClassField() {
        var field = new TwinClassFieldEntity();
        field.setId(UUID.randomUUID());
        field.setKey("listField");
        return field;
    }

    private DataListOptionEntity buildOption(UUID optionId) {
        var option = new DataListOptionEntity();
        option.setId(optionId);
        return option;
    }

    @Nested
    class AppendDescriptor {

        @Test
        void appendDescriptor_setsDefaultOptionId() throws ServiceException {
            var optionId = UUID.randomUUID();
            var props = new Properties();
            props.setProperty("defaultOptionId", optionId.toString());
            var descriptor = new FieldDescriptorList();

            initializer.appendDescriptor(props, descriptor);

            assertEquals(optionId, descriptor.defaultDataListOptionId());
        }
    }
}
