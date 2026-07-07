package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import static org.junit.jupiter.api.Assertions.*;

class FieldValueTextTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    @Nested
    class SetValue {

        @Test
        void setValue_nonBlankString_marksPresent() {
            var value = new FieldValueText(field);

            value.setValue("hello");

            assertEquals("hello", value.getValue());
            assertTrue(value.isDefined());
            assertFalse(value.isCleared());
        }

        @Test
        void setValue_emptyString_marksPresent() {
            var value = new FieldValueText(field);

            value.setValue("");

            // empty string is a value, not cleared (only null clears a Simple field)
            assertEquals("", value.getValue());
            assertTrue(value.isDefined());
            assertFalse(value.isCleared());
            assertTrue(value.isNotEmpty());
        }

        @Test
        void setValue_null_marksCleared() {
            var value = new FieldValueText(field);
            value.setValue("hello");

            value.setValue(null);

            assertNull(value.getValue());
            assertTrue(value.isCleared());
            assertTrue(value.isEmpty());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_exactMatch_returnsTrue() {
            var value = new FieldValueText(field);
            value.setValue("hello");

            assertTrue(value.hasValue("hello"));
        }

        @Test
        void hasValue_caseSensitiveMismatch_returnsFalse() {
            var value = new FieldValueText(field);
            value.setValue("Hello");

            assertFalse(value.hasValue("hello"));
        }

        @Test
        void hasValue_whenCleared_returnsFalse() {
            var value = new FieldValueText(field);
            value.setValue(null);

            assertFalse(value.hasValue("anything"));
        }
    }

    @Nested
    class CloneAndCopy {

        @Test
        void clone_isIndependentCopy() {
            var original = new FieldValueText(field);
            original.setValue("abc");

            var clone = original.clone();

            assertEquals(original, clone);
            ((FieldValueText) clone).setValue("xyz");
            assertEquals("abc", original.getValue());
        }

        @Test
        void copyValueTo_copiesValueOnly() {
            var src = new FieldValueText(field);
            src.setValue("abc");
            var dst = new FieldValueText(field);

            src.copyValueTo(dst);

            assertEquals("abc", dst.getValue());
            // the typed overload copies the value only; state is not copied, so dst stays UNDEFINED.
            assertTrue(dst.isUndefined());
        }
    }
}
