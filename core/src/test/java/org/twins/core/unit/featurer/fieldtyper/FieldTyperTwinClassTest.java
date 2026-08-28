package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldTwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.value.FieldValueTwinClassList;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldTyperTwinClassTest extends BaseUnitTest {

    @Mock
    private TwinClassService twinClassService;

    @Mock
    private TwinService twinService;

    private FieldTyperTwinClass fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperTwinClass();
        setField(fieldTyper, "twinClassService", twinClassService);
        setField(fieldTyper, "twinService", twinService);
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

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsTwinClassListDescriptor() throws ServiceException {
            // Intended: a bare FieldDescriptorTwinClassList is produced (no params to propagate).
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            var descriptor = fieldTyper.getFieldDescriptor(classField, new Properties());

            assertInstanceOf(FieldDescriptorTwinClassList.class, descriptor);
        }
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_loadsStoredTwinClassesForField() throws ServiceException {
            // Intended: deserialization reads the twin's stored twin-class entities for this field into the value.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var tc1 = new TwinClassEntity().setId(UUID.randomUUID());
            var tc2 = new TwinClassEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var stored1 = new TwinFieldTwinClassEntity().setId(UUID.randomUUID()).setTwinClass(tc1).setTwinClassId(tc1.getId()).setTwinClassFieldId(classField.getId());
            var stored2 = new TwinFieldTwinClassEntity().setId(UUID.randomUUID()).setTwinClass(tc2).setTwinClassId(tc2.getId()).setTwinClassFieldId(classField.getId());
            twin.setTwinFieldTwinClassKit(new KitGrouped<>(List.of(stored1, stored2), TwinFieldTwinClassEntity::getId, TwinFieldTwinClassEntity::getTwinClassFieldId));

            FieldValueTwinClassList result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            // KitGrouped.getGrouped is backed by a HashMap → order is not guaranteed; assert membership.
            assertEquals(2, result.getItems().size());
            assertTrue(result.getItems().contains(tc1));
            assertTrue(result.getItems().contains(tc2));
        }

        @Test
        void deserializeValue_noStoredEntries_returnsEmptyValue() throws ServiceException {
            // Intended: a twin with no stored twin-class rows for the field yields an empty value.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity()
                    .setId(UUID.randomUUID())
                    .setTwinFieldTwinClassKit(KitGrouped.EMPTY);

            FieldValueTwinClassList result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_singleNewTwinClass_addsFieldEntity() throws ServiceException {
            // Intended: with no prior storage, serializing a single selected twin class inserts a new
            // TwinFieldTwinClassEntity bound to the twin + twin-class field + selected twin class.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var selected = new TwinClassEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            // empty storage kit → isSingleValueAdd branch
            twin.setTwinFieldTwinClassKit(new KitGrouped<>(List.of(), TwinFieldTwinClassEntity::getId, TwinFieldTwinClassEntity::getTwinClassFieldId));
            when(twinClassService.findEntitiesSafe(List.of(selected.getId())))
                    .thenReturn(new Kit<>(List.of(selected), TwinClassEntity::getId));
            var value = new FieldValueTwinClassList(classField);
            value.add(selected);
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(new Properties(), twin, value, collector);

            assertTrue(collector.hasChanges());
            var saved = collector.getSaveEntitiesAll().stream()
                    .filter(e -> e instanceof TwinFieldTwinClassEntity)
                    .map(e -> (TwinFieldTwinClassEntity) e)
                    .toList();
            assertEquals(1, saved.size());
            assertEquals(selected.getId(), saved.get(0).getTwinClassId());
            assertSame(selected, saved.get(0).getTwinClass());
            assertEquals(twin.getId(), saved.get(0).getTwinId());
            assertEquals(classField.getId(), saved.get(0).getTwinClassFieldId());
        }
    }
}
