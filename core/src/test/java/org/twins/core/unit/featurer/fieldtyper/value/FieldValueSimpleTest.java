package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueSimple;
import org.twins.core.featurer.fieldtyper.value.FieldValueStated;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldValueSimple<T> is the FieldValueStated-backed single-typed-value variant
 * (the most common base for primitive-ish fields). setValue transitions state,
 * hasValue string-compares, onClear/onUndefine null the value.
 *
 * Stub subclass makes the typed setValue visible to the test.
 */
class FieldValueSimpleTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    public static class StubSimple extends FieldValueSimple<String> {
        public StubSimple(TwinClassFieldEntity field) { super(field); }

        @Override
        public StubSimple newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
            return new StubSimple(newTwinClassFieldEntity);
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
            var value = new StubSimple(field);

            var returned = value.setValue("abc");

            assertSame(value, returned);
            assertEquals("abc", value.getValue());
            assertFalse(value.isUndefined());
            assertFalse(value.isCleared());
        }

        @Test
        void setValue_null_clears() {
            var value = new StubSimple(field);
            value.setValue("abc");

            value.setValue(null);

            assertNull(value.getValue());
            assertTrue(value.isCleared());
        }

        @Test
        void setValue_returnsFieldValue_notGenericSimple() {
            // The override declares return type FieldValue (the supertype),
            // not FieldValueSimple<T>. Pinned so accidental generic-tightening is caught.
            var value = new StubSimple(field);

            var returned = value.setValue("abc");

            assertInstanceOf(FieldValue.class, returned);
            assertSame(value, returned);
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingString_returnsTrue() {
            var value = new StubSimple(field);
            value.setValue("abc");

            assertTrue(value.hasValue("abc"));
        }

        @Test
        void hasValue_mismatchingString_returnsFalse() {
            var value = new StubSimple(field);
            value.setValue("abc");

            assertFalse(value.hasValue("xyz"));
        }

        @Test
        void hasValue_whenValueNull_returnsFalseForNonNullArg() {
            var value = new StubSimple(field);

            assertFalse(value.hasValue("abc"));
        }

        @Test
        void hasValue_whenValueNullAndArgNull_returnsTrue() {
            // StringUtils.equals(null, null) == true — pinned null-safe contract
            var value = new StubSimple(field);

            assertTrue(value.hasValue(null));
        }
    }

    @Nested
    class StateLifecycle {

        @Test
        void newInstance_isUndefined() {
            var value = new StubSimple(field);

            assertTrue(value.isUndefined());
        }

        @Test
        void clear_wipesValue() {
            var value = new StubSimple(field);
            value.setValue("abc");

            value.clear();

            assertNull(value.getValue());
            assertTrue(value.isCleared());
        }

        @Test
        void undefine_wipesValue() {
            var value = new StubSimple(field);
            value.setValue("abc");

            value.undefine();

            assertNull(value.getValue());
            assertTrue(value.isUndefined());
        }
    }

    @Nested
    class CopyValueTo {

        @Test
        void copyValueTo_viaFieldValueOverload_copiesValueAndState() {
            // Going through FieldValueStated.copyValueTo(FieldValue) MUST copy both
            // the value (via the typed override) and the state (set by the caller).
            var src = new StubSimple(field);
            src.setValue("abc");
            var dst = new StubSimple(field);

            src.copyValueTo((FieldValue) dst);

            assertEquals("abc", dst.getValue());
            assertFalse(dst.isUndefined());
        }

        @Test
        void copyValueTo_typedOverload_copiesValueOnly() {
            // Contract: copyValueTo(FieldValueStated) copies the value only. State is applied separately
            // by FieldValueStated.copyValueTo(FieldValue), so calling the typed overload directly leaves
            // a fresh destination UNDEFINED. See TODO on FieldValueStated.copyValueTo(FieldValueStated).
            var src = new StubSimple(field);
            src.setValue("abc");
            var dst = new StubSimple(field);

            src.copyValueTo((FieldValueStated) dst);

            assertEquals("abc", dst.getValue());
            assertTrue(dst.isUndefined());
        }
    }

    @Nested
    class Clone {

        @Test
        void clone_viaFieldValueStated_isEqualToOriginalByValue() {
            var original = new StubSimple(field);
            original.setValue("abc");

            var clone = original.clone();

            assertNotSame(original, clone);
            assertEquals("abc", ((StubSimple) clone).getValue());
        }
    }
}
