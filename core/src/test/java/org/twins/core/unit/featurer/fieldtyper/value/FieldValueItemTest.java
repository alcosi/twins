package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueItem;

import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldValueItem<T> is the FieldValueStated-backed single-item variant that supports
 * a "nullify marker" id (treated as a clear). setValue transitions to PRESENT or CLEARED;
 * hasValue parses the input as a UUID and compares against the stored item's id.
 */
class FieldValueItemTest extends BaseUnitTest {

    private static final UUID NULLIFY_MARKER =
            UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    private TwinClassFieldEntity field;

    static class Holder {
        final UUID id;
        Holder(UUID id) { this.id = id; }
    }

    static class StubItemValue extends FieldValueItem<Holder> {
        StubItemValue(TwinClassFieldEntity field) { super(field); }

        @Override
        protected Function<Holder, UUID> itemGetIdFunction() {
            return h -> h.id;
        }

        @Override
        public StubItemValue newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
            return new StubItemValue(newTwinClassFieldEntity);
        }
    }

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    @Nested
    class SetValue {

        @Test
        void setValue_nonNull_transitionsToPresent() {
            var value = new StubItemValue(field);

            var returned = value.setValue(new Holder(UUID.randomUUID()));

            assertSame(value, returned);
            assertNotNull(value.getValue());
            assertFalse(value.isUndefined());
            assertFalse(value.isCleared());
        }

        @Test
        void setValue_null_clears() {
            var value = new StubItemValue(field);
            value.setValue(new Holder(UUID.randomUUID()));

            value.setValue(null);

            assertNull(value.getValue());
            assertTrue(value.isCleared());
        }

        @Test
        void setValue_nullifyMarkerId_clears() {
            var value = new StubItemValue(field);
            value.setValue(new Holder(UUID.randomUUID()));

            value.setValue(new Holder(NULLIFY_MARKER));

            assertNull(value.getValue());
            assertTrue(value.isCleared());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingIdString_returnsTrue() {
            var id = UUID.randomUUID();
            var value = new StubItemValue(field);
            value.setValue(new Holder(id));

            assertTrue(value.hasValue(id.toString()));
        }

        @Test
        void hasValue_mismatchingIdString_returnsFalse() {
            var value = new StubItemValue(field);
            value.setValue(new Holder(UUID.randomUUID()));

            assertFalse(value.hasValue(UUID.randomUUID().toString()));
        }

        @Test
        void hasValue_nonUuidString_returnsFalse() {
            var value = new StubItemValue(field);
            value.setValue(new Holder(UUID.randomUUID()));

            assertFalse(value.hasValue("not-a-uuid"));
        }

        @Test
        void hasValue_whenValueNull_returnsFalse() {
            var value = new StubItemValue(field);

            assertFalse(value.hasValue(UUID.randomUUID().toString()));
        }
    }

    @Nested
    class StateLifecycle {

        @Test
        void clear_wipesValue() {
            var value = new StubItemValue(field);
            value.setValue(new Holder(UUID.randomUUID()));

            value.clear();

            assertNull(value.getValue());
            assertTrue(value.isCleared());
        }

        @Test
        void undefine_wipesValue() {
            var value = new StubItemValue(field);
            value.setValue(new Holder(UUID.randomUUID()));

            value.undefine();

            assertNull(value.getValue());
            assertTrue(value.isUndefined());
        }
    }

    @Nested
    class CopyValueTo {

        @Test
        void copyValueTo_copiesValueReference() {
            var holder = new Holder(UUID.randomUUID());
            var src = new StubItemValue(field);
            src.setValue(holder);
            var dst = new StubItemValue(field);

            src.copyValueTo(dst);

            assertSame(holder, dst.getValue());
        }
    }

    @Nested
    class NewInstance {

        @Test
        void newInstance_yieldsFreshItem() {
            var src = new StubItemValue(field);
            src.setValue(new Holder(UUID.randomUUID()));

            var created = src.newInstance(field);

            assertInstanceOf(StubItemValue.class, created);
            assertNotSame(src, created);
            assertNull(((StubItemValue) created).getValue());
        }
    }
}
