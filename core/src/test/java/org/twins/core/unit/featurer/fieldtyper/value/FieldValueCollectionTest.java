package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueCollection;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * FieldValueCollection<T> is the mutable base for collection-backed values.
 * State is derived from the collection itself:
 *   collection == null  -> UNDEFINED
 *   collection empty    -> CLEARED
 *   otherwise           -> PRESENT
 *
 * Stub uses a simple holder with a UUID id, so itemGetIdFunction is trivial.
 */
class FieldValueCollectionTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    static class Holder {
        final UUID id;
        Holder(UUID id) { this.id = id; }
    }

    static class StubCollectionValue extends FieldValueCollection<Holder> {
        StubCollectionValue(TwinClassFieldEntity field) { super(field); }

        @Override
        protected Function<Holder, UUID> itemGetIdFunction() {
            return h -> h.id;
        }

        @Override
        public FieldValueCollection<Holder> add(Holder newItem) {
            return super.add(newItem);
        }

        @Override
        public StubCollectionValue newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
            return new StubCollectionValue(newTwinClassFieldEntity);
        }
    }

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    private Holder holder(UUID id) {
        return new Holder(id);
    }

    @Nested
    class DerivedState {

        @Test
        void newCollection_isUndefinedAndEmpty() {
            var value = new StubCollectionValue(field);

            assertTrue(value.isUndefined());
            assertFalse(value.isCleared());
            assertTrue(value.isEmpty());
            assertFalse(value.isDefined());
            assertEquals(0, value.size());
        }

        @Test
        void isUndefined_afterAdd_becomesFalse() {
            var value = new StubCollectionValue(field);
            value.add(holder(UUID.randomUUID()));

            assertFalse(value.isUndefined());
            assertFalse(value.isCleared());
            assertFalse(value.isEmpty());
        }

        @Test
        void isCleared_whenCollectionEmptied_returnsTrue() {
            var value = new StubCollectionValue(field);
            value.add(holder(UUID.randomUUID()));

            value.clear();

            // after clear the collection is non-null and empty => CLEARED
            assertFalse(value.isUndefined());
            assertTrue(value.isCleared());
            assertTrue(value.isEmpty());
        }
    }

    @Nested
    class Add {

        @Test
        void add_nullItem_isNoOp() {
            var value = new StubCollectionValue(field);

            var returned = value.add(null);

            assertSame(value, returned);
            assertEquals(0, value.size());
            assertTrue(value.isUndefined());
        }

        @Test
        void add_nullifyMarkerItem_clears() {
            var value = new StubCollectionValue(field);
            value.add(holder(UUID.randomUUID()));

            value.add(holder(nullifyMarker()));

            assertTrue(value.isCleared());
            assertEquals(0, value.size());
        }

        @Test
        void add_appendsToCollection() {
            var value = new StubCollectionValue(field);

            value.add(holder(UUID.randomUUID()));
            value.add(holder(UUID.randomUUID()));

            assertEquals(2, value.size());
        }
    }

    @Nested
    class SetItems {

        @Test
        void setItems_nonEmpty_copiesIntoNewList() {
            var value = new StubCollectionValue(field);
            var src = List.of(holder(UUID.randomUUID()));

            value.setItems(src);

            assertEquals(1, value.size());
            // mutations to source list must not bleed into value
            assertNotSame(src, value.getItems());
        }

        @Test
        void setItems_null_clears() {
            var value = new StubCollectionValue(field);
            value.add(holder(UUID.randomUUID()));

            value.setItems(null);

            assertTrue(value.isCleared());
        }

        @Test
        void setItems_empty_clears() {
            var value = new StubCollectionValue(field);
            value.add(holder(UUID.randomUUID()));

            value.setItems(List.of());

            assertTrue(value.isCleared());
        }

        @Test
        void setItems_nullifyMarker_clears() {
            var value = new StubCollectionValue(field);

            value.setItems(List.of(holder(nullifyMarker())));

            assertTrue(value.isCleared());
        }
    }

    @Nested
    class Accessors {

        @Test
        void getItems_whenUndefined_returnsNull() {
            // contract: collection field stays null until first set; getItems is raw accessor
            var value = new StubCollectionValue(field);

            assertNull(value.getItems());
        }

        @Test
        void getItemsOrEmpty_whenUndefined_returnsEmptyList() {
            var value = new StubCollectionValue(field);

            var items = value.getItemsOrEmpty();

            assertNotNull(items);
            assertTrue(items.isEmpty());
        }

        @Test
        void getItemsOrEmpty_whenItemsPresent_returnsSameCollection() {
            var value = new StubCollectionValue(field);
            value.add(holder(UUID.randomUUID()));

            assertSame(value.getItems(), value.getItemsOrEmpty());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingIdString_returnsTrue() {
            var id = UUID.randomUUID();
            var value = new StubCollectionValue(field);
            value.add(holder(id));

            assertTrue(value.hasValue(id.toString()));
        }

        @Test
        void hasValue_nonUuidString_returnsFalse() {
            var value = new StubCollectionValue(field);
            value.add(holder(UUID.randomUUID()));

            assertFalse(value.hasValue("nope"));
        }
    }

    @Nested
    class Undefine {

        @Test
        void undefine_dropsCollection_andIsChained() {
            var value = new StubCollectionValue(field);
            value.add(holder(UUID.randomUUID()));

            var returned = value.undefine();

            assertSame(value, returned);
            assertNull(value.getItems());
            assertTrue(value.isUndefined());
            assertEquals(0, value.size());
        }
    }

    @Nested
    class Clear {

        // =====================================================================
        // RED — encodes PROD BUG #2 documented in the audit:
        //   FieldValueCollection.clear() ends with `return null;` while every
        //   sibling in the hierarchy (FieldValueStated#clear, FieldValueInvisible#clear
        //   also broken, FieldValueStated-family subclasses) returns `this`.
        //   Intended contract: clear() returns the value itself for chaining.
        // =====================================================================
        @Test
        void clear_returnsThisForChaining() {
            var value = new StubCollectionValue(field);
            value.add(holder(UUID.randomUUID()));

            var returned = value.clear();

            assertSame(value, returned);
        }

        @Test
        void clear_onUndefined_populatesEmptyList() {
            var value = new StubCollectionValue(field);

            value.clear();

            // an undefined value that is cleared transitions to CLEARED (non-null empty list),
            // because FieldValueCollection.clear() deliberately avoids Collections.emptyList()
            assertTrue(value.isCleared());
            assertNotNull(value.getItems());
        }

        @Test
        void clear_onPopulated_emptiesInPlace() {
            var value = new StubCollectionValue(field);
            value.add(holder(UUID.randomUUID()));
            value.add(holder(UUID.randomUUID()));
            var originalCollection = value.getItems();

            value.clear();

            assertTrue(originalCollection.isEmpty());
            assertTrue(value.isCleared());
        }
    }

    @Nested
    class CopyValueTo {

        @Test
        void copyValueTo_emptyDestination_createsNewListCopy() {
            var src = new StubCollectionValue(field);
            src.add(holder(UUID.randomUUID()));
            var dst = new StubCollectionValue(field);

            src.copyValueTo(dst);

            assertEquals(1, dst.size());
            assertNotSame(src.getItems(), dst.getItems());
        }

        @Test
        void copyValueTo_populatedDestination_clearsAndOverwrites() {
            var src = new StubCollectionValue(field);
            src.add(holder(UUID.randomUUID()));
            var dst = new StubCollectionValue(field);
            dst.add(holder(UUID.randomUUID()));
            var dstOriginalList = dst.getItems();

            src.copyValueTo(dst);

            assertEquals(1, dst.size());
            // in-place clear+addAll keeps the same list instance
            assertSame(dstOriginalList, dst.getItems());
        }

        @Test
        void copyValueTo_nullSource_setsDestinationNull() {
            var src = new StubCollectionValue(field);
            var dst = new StubCollectionValue(field);
            dst.add(holder(UUID.randomUUID()));

            src.copyValueTo(dst);

            assertNull(dst.getItems());
            assertTrue(dst.isUndefined());
        }
    }

    private static UUID nullifyMarker() {
        return UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    }
}
