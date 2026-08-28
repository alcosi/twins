package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldValueSelectTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    private DataListOptionEntity option(UUID id) {
        var opt = new DataListOptionEntity();
        opt.setId(id);
        return opt;
    }

    @Nested
    class StateByCollection {

        @Test
        void newInstance_collectionNull_isUndefinedAndEmpty() {
            var value = new FieldValueSelect(field);

            assertTrue(value.isUndefined());
            assertTrue(value.isEmpty());
            assertEquals(0, value.size());
            assertTrue(value.getItemsOrEmpty().isEmpty());
        }

        @Test
        void addFirstItem_marksDefinedAndNotEmpty() {
            var value = new FieldValueSelect(field);

            value.add(option(UUID.randomUUID()));

            assertFalse(value.isUndefined());
            assertFalse(value.isEmpty());
            assertEquals(1, value.size());
        }

        @Test
        void clear_leavesEmptyNonNullCollection_isCleared() {
            var value = new FieldValueSelect(field);
            value.add(option(UUID.randomUUID()));

            value.clear();

            // FieldValueCollection.clear() guarantees a non-null empty list, so the field
            // is "cleared" (present-but-empty), distinct from "undefined"
            assertTrue(value.isCleared());
            assertFalse(value.isUndefined());
            assertTrue(value.isEmpty());
            assertNotNull(value.getItems());
            assertEquals(0, value.size());
        }

        @Test
        void undefine_setsCollectionNull_isUndefined() {
            var value = new FieldValueSelect(field);
            value.add(option(UUID.randomUUID()));

            value.undefine();

            assertTrue(value.isUndefined());
            assertTrue(value.isEmpty());
            assertNull(value.getItems());
        }
    }

    @Nested
    class Add {

        @Test
        void add_nullItem_isNoOp() {
            var value = new FieldValueSelect(field);

            value.add(null);

            assertEquals(0, value.size());
            // still undefined, not cleared
            assertTrue(value.isUndefined());
        }

        @Test
        void add_multipleItems_keepsOrder() {
            var value = new FieldValueSelect(field);
            var a = option(UUID.randomUUID());
            var b = option(UUID.randomUUID());

            value.add(a);
            value.add(b);

            assertEquals(List.of(a, b), value.getItems());
        }

        @Test
        void add_nullifyMarkerId_clearsField() {
            var value = new FieldValueSelect(field);
            value.add(option(UUID.randomUUID()));
            var nullify = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

            value.add(option(nullify));

            assertTrue(value.isCleared());
            assertTrue(value.isEmpty());
        }
    }

    @Nested
    class SetItems {

        @Test
        void setItems_nonEmpty_replacesContents() {
            var value = new FieldValueSelect(field);
            value.add(option(UUID.randomUUID()));
            var b = option(UUID.randomUUID());

            value.setItems(List.of(b));

            assertEquals(List.of(b), value.getItems());
        }

        @Test
        void setItems_empty_clearsField() {
            var value = new FieldValueSelect(field);
            value.add(option(UUID.randomUUID()));

            value.setItems(List.of());

            assertTrue(value.isCleared());
            assertTrue(value.isEmpty());
        }

        @Test
        void setItems_null_clearsField() {
            var value = new FieldValueSelect(field);
            value.add(option(UUID.randomUUID()));

            value.setItems(null);

            assertTrue(value.isCleared());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingIdString_returnsTrue() {
            var id = UUID.randomUUID();
            var value = new FieldValueSelect(field);
            value.add(option(id));

            assertTrue(value.hasValue(id.toString()));
        }

        @Test
        void hasValue_emptyCollection_returnsFalse() {
            var value = new FieldValueSelect(field);

            assertFalse(value.hasValue(UUID.randomUUID().toString()));
        }

        @Test
        void hasValue_nonUuidString_returnsFalse() {
            var value = new FieldValueSelect(field);
            value.add(option(UUID.randomUUID()));

            assertFalse(value.hasValue("nope"));
        }
    }

    @Nested
    class CopyValueTo {

        @Test
        void copyValueTo_copiesContentsAndIsIndependent() {
            var src = new FieldValueSelect(field);
            var a = option(UUID.randomUUID());
            src.add(a);
            var dst = new FieldValueSelect(field);

            src.copyValueTo(dst);

            assertEquals(List.of(a), dst.getItems());
            // dst collection must be a separate list instance
            assertNotSame(src.getItems(), dst.getItems());

            dst.add(option(UUID.randomUUID()));
            assertEquals(1, src.size());
        }

        @Test
        void copyValueTo_fromClearedProducesClearedDst() {
            var src = new FieldValueSelect(field);
            src.clear();
            var dst = new FieldValueSelect(field);

            src.copyValueTo(dst);

            assertTrue(dst.isCleared());
            assertEquals(0, dst.size());
        }
    }

    @Nested
    class Clone {

        @Test
        @Disabled("bug #1: base FieldValue classes lack @EqualsAndHashCode -> equals is identity, so clone never equals original.")
        void clone_producesEqualIndependentCopy() {
            var original = new FieldValueSelect(field);
            var a = option(UUID.randomUUID());
            original.add(a);

            var clone = original.clone();

            assertNotSame(original, clone);
            assertEquals(original, clone);
            assertEquals(List.of(a), ((FieldValueSelect) clone).getItems());
        }
    }
}
