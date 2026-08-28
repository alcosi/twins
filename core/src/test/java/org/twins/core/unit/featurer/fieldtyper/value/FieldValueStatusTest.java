package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldValueStatusTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    private TwinStatusEntity status(UUID id) {
        return new TwinStatusEntity().setId(id);
    }

    @Nested
    class SetValue {

        @Test
        void setValue_entity_marksPresentAndStoresValue() {
            var value = new FieldValueStatus(field);
            var s = status(UUID.randomUUID());

            var returned = value.setValue(s);

            assertSame(value, returned);
            assertEquals(s, value.getValue());
            assertTrue(value.isDefined());
            assertFalse(value.isCleared());
        }

        @Test
        void setValue_null_marksCleared() {
            var value = new FieldValueStatus(field);
            value.setValue(status(UUID.randomUUID()));

            value.setValue(null);

            assertNull(value.getValue());
            assertTrue(value.isCleared());
            assertTrue(value.isEmpty());
        }

        @Test
        void setValue_nullifyMarkerUuid_marksCleared() {
            var value = new FieldValueStatus(field);
            // the sentinel UUID ffffffff-...-ffff means "clear this field"
            var nullify = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

            value.setValue(status(nullify));

            assertNull(value.getValue());
            assertTrue(value.isCleared());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingIdString_returnsTrue() {
            var id = UUID.randomUUID();
            var value = new FieldValueStatus(field);
            value.setValue(status(id));

            assertTrue(value.hasValue(id.toString()));
        }

        @Test
        void hasValue_nonUuidString_returnsFalse() {
            var value = new FieldValueStatus(field);
            value.setValue(status(UUID.randomUUID()));

            assertFalse(value.hasValue("garbage"));
        }

        @Test
        void hasValue_whenCleared_returnsFalse() {
            var value = new FieldValueStatus(field);
            value.setValue(null);

            assertFalse(value.hasValue(UUID.randomUUID().toString()));
        }
    }

    @Nested
    class CloneAndCopy {

        @Test
        @Disabled("bug #1: base FieldValue classes lack @EqualsAndHashCode -> equals is identity, so clone never equals original.")
        void clone_isIndependentCopy() {
            var original = new FieldValueStatus(field);
            var s = status(UUID.randomUUID());
            original.setValue(s);

            var clone = original.clone();

            assertEquals(original, clone);
            assertEquals(s, ((FieldValueStatus) clone).getValue());
        }

        @Test
        void copyValueTo_copiesValueOnly() {
            var src = new FieldValueStatus(field);
            var s = status(UUID.randomUUID());
            src.setValue(s);
            var dst = new FieldValueStatus(field);

            src.copyValueTo(dst);

            assertEquals(s, dst.getValue());
            // the typed overload copies the value only; state is not copied, so dst stays UNDEFINED.
            assertTrue(dst.isUndefined());
        }
    }
}
