package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueCollectionImmutable;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * FieldValueCollectionImmutable<T> is the FieldValueStated-backed immutable variant.
 * Unlike FieldValueCollection, state is tracked explicitly via the FieldValueStated
 * state field (and the collection is wiped to null on nullify/clear).
 */
class FieldValueCollectionImmutableTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    static class Holder {
        final UUID id;
        Holder(UUID id) { this.id = id; }
    }

    static class StubValue extends FieldValueCollectionImmutable<Holder> {
        StubValue(TwinClassFieldEntity field) { super(field); }

        @Override
        protected Function<Holder, UUID> itemGetIdFunction() {
            return h -> h.id;
        }

        @Override
        public StubValue newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
            return new StubValue(newTwinClassFieldEntity);
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
    class InitialState {

        @Test
        void newInstance_isUndefined() {
            var value = new StubValue(field);

            assertTrue(value.isUndefined());
            assertFalse(value.isCleared());
            assertEquals(0, value.size());
        }
    }

    @Nested
    class Add {

        @Test
        void add_nullItem_isNoOpStaysUndefined() {
            var value = new StubValue(field);

            var returned = value.add(null);

            assertSame(value, returned);
            assertTrue(value.isUndefined());
        }

        @Test
        void add_singleItem_transitionsToPresent() {
            var value = new StubValue(field);

            value.add(holder(UUID.randomUUID()));

            assertFalse(value.isUndefined());
            assertFalse(value.isCleared());
            assertEquals(1, value.size());
        }

        @Test
        void add_nullifyMarker_clearsAndWipesCollection() {
            var value = new StubValue(field);
            value.add(holder(UUID.randomUUID()));

            value.add(holder(nullifyMarker()));

            assertTrue(value.isCleared());
            assertFalse(value.isUndefined());
            assertEquals(0, value.size());
        }
    }

    @Nested
    class SetItems {

        @Test
        void setItems_nonEmpty_transitionsToPresent() {
            var value = new StubValue(field);

            value.setItems(List.of(holder(UUID.randomUUID())));

            assertFalse(value.isUndefined());
            assertEquals(1, value.size());
        }

        @Test
        void setItems_empty_clears() {
            var value = new StubValue(field);
            value.add(holder(UUID.randomUUID()));

            value.setItems(List.of());

            assertTrue(value.isCleared());
            assertEquals(0, value.size());
        }

        @Test
        void setItems_nullifyMarker_clears() {
            var value = new StubValue(field);

            value.setItems(List.of(holder(nullifyMarker())));

            assertTrue(value.isCleared());
        }
    }

    @Nested
    class GetItems {

        @Test
        void getItems_whenUndefined_returnsImmutableEmpty() {
            var value = new StubValue(field);

            var items = value.getItems();

            assertNotNull(items);
            assertTrue(items.isEmpty());
            assertThrows(UnsupportedOperationException.class, () -> items.add(holder(UUID.randomUUID())));
        }

        @Test
        void getItems_returnsUnmodifiableView() {
            var value = new StubValue(field);
            value.add(holder(UUID.randomUUID()));

            var items = value.getItems();

            assertThrows(UnsupportedOperationException.class, () -> items.add(holder(UUID.randomUUID())));
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingId_returnsTrue() {
            var id = UUID.randomUUID();
            var value = new StubValue(field);
            value.add(holder(id));

            assertTrue(value.hasValue(id.toString()));
        }

        @Test
        void hasValue_nonUuidString_returnsFalse() {
            var value = new StubValue(field);
            value.add(holder(UUID.randomUUID()));

            assertFalse(value.hasValue("nope"));
        }
    }

    @Nested
    class StateTransitions {

        @Test
        void clear_setsClearedState() {
            var value = new StubValue(field);
            value.add(holder(UUID.randomUUID()));

            value.clear();

            assertTrue(value.isCleared());
        }

        @Test
        void undefine_setsUndefinedState() {
            var value = new StubValue(field);
            value.add(holder(UUID.randomUUID()));

            value.undefine();

            assertTrue(value.isUndefined());
        }
    }

    @Nested
    class CopyValueTo {

        @Test
        void copyValueTo_emptyDestination_copiesIntoNewListAndState() {
            var src = new StubValue(field);
            src.add(holder(UUID.randomUUID()));
            src.add(holder(UUID.randomUUID()));
            var dst = new StubValue(field);

            src.copyValueTo(dst);

            assertEquals(2, dst.size());
            // FieldValueStated.copyValueTo(FieldValue) copies the state too
            assertFalse(dst.isUndefined());
        }

        @Test
        void copyValueTo_populatedDestination_overwritesInPlace() {
            var src = new StubValue(field);
            src.add(holder(UUID.randomUUID()));
            var dst = new StubValue(field);
            dst.add(holder(UUID.randomUUID()));
            dst.add(holder(UUID.randomUUID()));

            src.copyValueTo(dst);

            assertEquals(1, dst.size());
        }
    }

    private static UUID nullifyMarker() {
        return UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    }
}
