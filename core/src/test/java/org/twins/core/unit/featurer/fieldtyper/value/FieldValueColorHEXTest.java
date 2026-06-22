package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueColorHEX;

import static org.junit.jupiter.api.Assertions.*;

class FieldValueColorHEXTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    @Nested
    class SetValue {

        @Test
        void setValue_hexString_marksPresent() {
            var value = new FieldValueColorHEX(field);

            value.setValue("#FF00AA");

            assertEquals("#FF00AA", value.getValue());
            assertTrue(value.isDefined());
        }

        @Test
        void setValue_null_marksCleared() {
            var value = new FieldValueColorHEX(field);
            value.setValue("#FF00AA");

            value.setValue(null);

            assertNull(value.getValue());
            assertTrue(value.isCleared());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_caseSensitiveMatch() {
            var value = new FieldValueColorHEX(field);
            value.setValue("#FF00AA");

            assertTrue(value.hasValue("#FF00AA"));
            // hex is conventionally case-sensitive here; lowercase must not match uppercase
            assertFalse(value.hasValue("#ff00aa"));
        }
    }

    @Nested
    class CloneAndCopy {

        @Test
        void clone_isIndependentCopy() {
            var original = new FieldValueColorHEX(field);
            original.setValue("#00FF00");

            var clone = original.clone();

            assertEquals(original, clone);
            ((FieldValueColorHEX) clone).setValue("#000000");
            assertEquals("#00FF00", original.getValue());
        }
    }
}
