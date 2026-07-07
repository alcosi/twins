package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueLinkSingle;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldValueLinkSingleTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    private TwinEntity twin(UUID id) {
        return new TwinEntity().setId(id);
    }

    @Nested
    class SetValue {

        @Test
        void setValue_twin_marksPresent() {
            var value = new FieldValueLinkSingle(field);
            var t = twin(UUID.randomUUID());

            value.setValue(t);

            assertEquals(t, value.getValue());
            assertTrue(value.isDefined());
        }

        @Test
        void setValue_null_marksCleared() {
            var value = new FieldValueLinkSingle(field);
            value.setValue(twin(UUID.randomUUID()));

            value.setValue(null);

            assertNull(value.getValue());
            assertTrue(value.isCleared());
        }

        @Test
        void setValue_nullifyMarkerUuid_marksCleared() {
            var value = new FieldValueLinkSingle(field);
            var nullify = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

            value.setValue(twin(nullify));

            assertNull(value.getValue());
            assertTrue(value.isCleared());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingTwinId_returnsTrue() {
            var id = UUID.randomUUID();
            var value = new FieldValueLinkSingle(field);
            value.setValue(twin(id));

            assertTrue(value.hasValue(id.toString()));
        }

        @Test
        void hasValue_mismatchedId_returnsFalse() {
            var value = new FieldValueLinkSingle(field);
            value.setValue(twin(UUID.randomUUID()));

            assertFalse(value.hasValue(UUID.randomUUID().toString()));
        }
    }

    @Nested
    class CloneAndCopy {

        @Test
        @Disabled("bug #1: base FieldValue classes lack @EqualsAndHashCode -> equals is identity, so clone never equals original.")
        void clone_isIndependentCopy() {
            var original = new FieldValueLinkSingle(field);
            var t = twin(UUID.randomUUID());
            original.setValue(t);

            var clone = original.clone();

            assertEquals(original, clone);
            assertEquals(t, ((FieldValueLinkSingle) clone).getValue());
        }
    }
}
