package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueTwinClassList;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * FieldValueTwinClassList is a FieldValueCollectionImmutable<TwinClassEntity>.
 * id function is TwinClassEntity::getId; everything else is inherited.
 */
class FieldValueTwinClassListTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    private TwinClassEntity twinClass(UUID id) {
        var e = mock(TwinClassEntity.class);
        when(e.getId()).thenReturn(id);
        return e;
    }

    @Nested
    class NewInstance {

        @Test
        void newInstance_yieldsTwinClassListWithNewField() {
            var src = new FieldValueTwinClassList(field);
            var newField = new TwinClassFieldEntity();

            var created = src.newInstance(newField);

            assertInstanceOf(FieldValueTwinClassList.class, created);
            assertSame(newField, created.getTwinClassField());
        }
    }

    @Nested
    class AddAndSetItems {

        @Test
        void add_singleItem_transitionsToPresent() {
            var value = new FieldValueTwinClassList(field);

            value.add(twinClass(UUID.randomUUID()));

            assertEquals(1, value.size());
            assertFalse(value.isUndefined());
        }

        @Test
        void add_nullItem_isNoOp() {
            var value = new FieldValueTwinClassList(field);

            value.add(null);

            assertEquals(0, value.size());
            assertTrue(value.isUndefined());
        }

        @Test
        void setItems_nonEmpty_transitionsToPresent() {
            var value = new FieldValueTwinClassList(field);

            value.setItems(List.of(twinClass(UUID.randomUUID()), twinClass(UUID.randomUUID())));

            assertEquals(2, value.size());
            assertFalse(value.isUndefined());
        }

        @Test
        void setItems_empty_clears() {
            var value = new FieldValueTwinClassList(field);
            value.add(twinClass(UUID.randomUUID()));

            value.setItems(List.of());

            assertTrue(value.isCleared());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingId_returnsTrue() {
            var id = UUID.randomUUID();
            var value = new FieldValueTwinClassList(field);
            value.add(twinClass(id));

            assertTrue(value.hasValue(id.toString()));
        }

        @Test
        void hasValue_nonUuidString_returnsFalse() {
            var value = new FieldValueTwinClassList(field);
            value.add(twinClass(UUID.randomUUID()));

            assertFalse(value.hasValue("not-a-uuid"));
        }
    }

    @Nested
    class CopyValueTo {

        @Test
        void copyValueTo_overwritesDestination() {
            var src = new FieldValueTwinClassList(field);
            src.add(twinClass(UUID.randomUUID()));
            var dst = new FieldValueTwinClassList(field);
            dst.add(twinClass(UUID.randomUUID()));

            src.copyValueTo(dst);

            assertEquals(1, dst.size());
        }
    }
}
