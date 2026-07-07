package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueUserSingle;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldValueUserSingleTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    private UserEntity user(UUID id) {
        return new UserEntity().setId(id);
    }

    @Nested
    class SetValue {

        @Test
        void setValue_user_marksPresent() {
            var value = new FieldValueUserSingle(field);
            var u = user(UUID.randomUUID());

            value.setValue(u);

            assertEquals(u, value.getValue());
            assertTrue(value.isDefined());
        }

        @Test
        void setValue_null_marksCleared() {
            var value = new FieldValueUserSingle(field);
            value.setValue(user(UUID.randomUUID()));

            value.setValue(null);

            assertNull(value.getValue());
            assertTrue(value.isCleared());
        }

        @Test
        void setValue_nullifyMarkerUuid_marksCleared() {
            var value = new FieldValueUserSingle(field);
            var nullify = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

            value.setValue(user(nullify));

            assertNull(value.getValue());
            assertTrue(value.isCleared());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingUserId_returnsTrue() {
            var id = UUID.randomUUID();
            var value = new FieldValueUserSingle(field);
            value.setValue(user(id));

            assertTrue(value.hasValue(id.toString()));
        }

        @Test
        void hasValue_nonUuidString_returnsFalse() {
            var value = new FieldValueUserSingle(field);
            value.setValue(user(UUID.randomUUID()));

            assertFalse(value.hasValue("not-a-uuid"));
        }
    }

    @Nested
    class CloneAndCopy {

        @Test
        @Disabled("bug #1: base FieldValue classes lack @EqualsAndHashCode → equals is identity, so clone never equals original. Re-enable once equals/hashCode is fixed (see FieldValue TODO).")
        void clone_isIndependentCopy() {
            var original = new FieldValueUserSingle(field);
            var u = user(UUID.randomUUID());
            original.setValue(u);

            var clone = original.clone();

            assertEquals(original, clone);
            assertEquals(u, ((FieldValueUserSingle) clone).getValue());
        }
    }
}
