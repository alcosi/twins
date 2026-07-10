package org.twins.core.featurer.fieldtyper;

import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.datalist.DataListService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldTyperSelectTest extends BaseUnitTest {

    @Mock
    private DataListService dataListService;

    @Mock
    private DataListOptionService dataListOptionService;

    private FieldTyperSelect fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperSelect();
        setField(fieldTyper, "dataListService", dataListService);
        setField(fieldTyper, "dataListOptionService", dataListOptionService);
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

    private Properties properties(UUID listId, String multiple, String supportCustom, String longListThreshold) {
        var props = new Properties();
        props.setProperty("listUUID", listId.toString());
        props.setProperty("multiple", multiple);
        props.setProperty("supportCustom", supportCustom);
        props.setProperty("longListThreshold", longListThreshold);
        return props;
    }

    private DataListOptionEntity option(UUID id, UUID dataListId) {
        return new DataListOptionEntity().setId(id).setDataListId(dataListId);
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_propagatesMultipleAndSupportCustom_noOptionsWhenThresholdZero() throws ServiceException {
            // Intended: with longListThreshold=0 the options list is left null (long-list flag) and the
            // multiple/supportCustom flags come straight from the typer params.
            var listId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            var descriptor = fieldTyper.getFieldDescriptor(classField, properties(listId, "true", "true", "0"));

            assertInstanceOf(FieldDescriptorList.class, descriptor);
            var list = (FieldDescriptorList) descriptor;
            assertTrue(list.multiple());
            assertTrue(list.supportCustom());
            assertEquals(listId, list.dataListId());
            assertNull(list.options());
        }

        @Test
        void getFieldDescriptor_loadsOptionsWhenListSizeBelowThreshold() throws ServiceException {
            // Intended: a small list (size below threshold) is eagerly loaded into descriptor.options.
            var listId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var opt = option(UUID.randomUUID(), listId);
            when(dataListService.countByDataListId(listId)).thenReturn(1);
            when(dataListService.findByDataListId(listId)).thenReturn(List.of(opt));

            var descriptor = (FieldDescriptorList) fieldTyper.getFieldDescriptor(classField, properties(listId, "false", "false", "10"));

            assertNotNull(descriptor.options());
            assertEquals(1, descriptor.options().size());
            assertSame(opt, descriptor.options().get(0));
        }

        @Test
        void getFieldDescriptor_leavesOptionsNullWhenListSizeAtOrAboveThreshold() throws ServiceException {
            // Intended: a list at/above the threshold is treated as a long list; options stay null.
            var listId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            when(dataListService.countByDataListId(listId)).thenReturn(50);

            var descriptor = (FieldDescriptorList) fieldTyper.getFieldDescriptor(classField, properties(listId, "false", "false", "50"));

            assertNull(descriptor.options());
        }
    }

    @Nested
    class Validate {

        @Test
        void validate_multipleOptionsWhenMultipleFalse_isInvalid() throws ServiceException {
            // Intended: a non-multi select must reject more than one option.
            var listId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueSelect(classField);
            value.add(option(UUID.randomUUID(), listId));
            value.add(option(UUID.randomUUID(), listId));

            ValidationResult result = fieldTyper.validate(properties(listId, "false", "false", "0"), twin, value);

            assertFalse(result.isValid());
        }

        @Test
        void validate_optionFromDifferentDataList_isInvalid() throws ServiceException {
            // Intended: an option whose dataListId differs from the field's list is rejected.
            var listId = UUID.randomUUID();
            var otherListId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueSelect(classField);
            value.add(option(UUID.randomUUID(), otherListId));

            ValidationResult result = fieldTyper.validate(properties(listId, "true", "false", "0"), twin, value);

            assertFalse(result.isValid());
        }

        @Test
        void validate_optionFromCorrectDataList_isValid() throws ServiceException {
            // Intended: an option matching the field's list passes validation.
            var listId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueSelect(classField);
            value.add(option(UUID.randomUUID(), listId));

            ValidationResult result = fieldTyper.validate(properties(listId, "true", "false", "0"), twin, value);

            assertTrue(result.isValid());
        }
    }

    @Nested
    class AllowMultiply {

        @Test
        void allowMultiply_reflectsMultipleParam() throws ServiceException {
            // Intended: the "multiple" param drives whether more than one option is permitted.
            var listId = UUID.randomUUID();
            var multi = properties(listId, "true", "false", "0");
            var single = properties(listId, "false", "false", "0");

            assertTrue(fieldTyper.allowMultiply(multi));
            assertFalse(fieldTyper.allowMultiply(single));
        }
    }
}
