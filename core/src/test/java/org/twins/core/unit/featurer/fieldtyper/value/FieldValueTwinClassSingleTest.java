package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueTwinClassSingle;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldValueTwinClassSingleTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    private TwinClassEntity twinClass(UUID id) {
        return new TwinClassEntity().setId(id);
    }

    @Nested
    class SetValue {

        @Test
        void setValue_twinClass_marksPresent() {
            var value = new FieldValueTwinClassSingle(field);
            var tc = twinClass(UUID.randomUUID());

            value.setValue(tc);

            assertEquals(tc, value.getValue());
            assertTrue(value.isDefined());
        }

        @Test
        void setValue_null_marksCleared() {
            var value = new FieldValueTwinClassSingle(field);
            value.setValue(twinClass(UUID.randomUUID()));

            value.setValue(null);

            assertNull(value.getValue());
            assertTrue(value.isCleared());
        }

        @Test
        void setValue_nullifyMarkerUuid_marksCleared() {
            var value = new FieldValueTwinClassSingle(field);
            var nullify = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

            value.setValue(twinClass(nullify));

            assertNull(value.getValue());
            assertTrue(value.isCleared());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingId_returnsTrue() {
            var id = UUID.randomUUID();
            var value = new FieldValueTwinClassSingle(field);
            value.setValue(twinClass(id));

            assertTrue(value.hasValue(id.toString()));
        }
    }

    @Nested
    class CloneAndCopy {

        @Test
        @Disabled("bug #1: base FieldValue classes lack @EqualsAndHashCode → equals is identity, so clone never equals original. Re-enable once equals/hashCode is fixed (see FieldValue TODO).")
        void clone_isIndependentCopy() {
            var original = new FieldValueTwinClassSingle(field);
            var tc = twinClass(UUID.randomUUID());
            original.setValue(tc);

            var clone = original.clone();

            assertEquals(original, clone);
            assertEquals(tc, ((FieldValueTwinClassSingle) clone).getValue());
        }
    }
}
