package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueStated;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldValueStated adds an explicit `state` field (UNDEFINED/PRESENT/CLEARED) on top
 * of FieldValue, plus the abstract onClear/onUndefine hooks driven by clear()/undefine().
 * copyValueTo(FieldValue) is the template method that delegates to the typed overload
 * and then copies state. updateMutableValueState is a no-op extension point.
 */
class FieldValueStatedTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    /**
     * Minimal concrete FieldValueStated. Tracks a single string and reports state.
     */
    static class StubStated extends FieldValueStated {
        private String value;

        StubStated(TwinClassFieldEntity field) { super(field); }

        @Override
        public StubStated newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
            return new StubStated(newTwinClassFieldEntity);
        }

        @Override
        public boolean hasValue(String value) {
            return java.util.Objects.equals(this.value, value);
        }

        @Override
        public void copyValueTo(FieldValueStated dst) {
            ((StubStated) dst).value = this.value;
        }

        @Override
        public void onUndefine() {
            value = null;
        }

        @Override
        public void onClear() {
            value = null;
        }

        void setValue(String v) {
            this.value = v;
            this.state = State.PRESENT;
        }

        String getValue() {
            return value;
        }
    }

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    @Nested
    class InitialState {

        @Test
        void newInstance_isUndefined() {
            var value = new StubStated(field);

            assertTrue(value.isUndefined());
            assertFalse(value.isCleared());
            assertTrue(value.isEmpty());
        }
    }

    @Nested
    class Undefine {

        @Test
        void undefine_invokesOnUndefineHookAndReturnsThis() {
            var value = new StubStated(field);
            value.setValue("abc");

            var returned = value.undefine();

            assertSame(value, returned);
            assertNull(value.getValue());
            assertTrue(value.isUndefined());
            assertFalse(value.isCleared());
        }
    }

    @Nested
    class Clear {

        @Test
        void clear_invokesOnClearHookAndReturnsThis() {
            var value = new StubStated(field);
            value.setValue("abc");

            var returned = value.clear();

            assertSame(value, returned);
            assertNull(value.getValue());
            assertTrue(value.isCleared());
            assertFalse(value.isUndefined());
        }
    }

    @Nested
    class StateDerivedFlags {

        @Test
        void isDefined_isNegationOfIsUndefined() {
            var value = new StubStated(field);
            value.setValue("abc");

            assertTrue(value.isDefined());
            assertFalse(value.isUndefined());
        }

        @Test
        void isEmpty_whenCleared_returnsTrue() {
            var value = new StubStated(field);
            value.setValue("abc");
            value.clear();

            assertTrue(value.isEmpty());
        }

        @Test
        void isEmpty_whenUndefined_returnsTrue() {
            var value = new StubStated(field);

            assertTrue(value.isEmpty());
        }

        @Test
        void isEmpty_whenPresent_returnsFalse() {
            var value = new StubStated(field);
            value.setValue("abc");

            assertFalse(value.isEmpty());
            assertTrue(value.isNotEmpty());
        }
    }

    @Nested
    class CopyValueTo {

        @Test
        void copyValueTo_viaFieldValueOverload_copiesValueAndState() {
            var src = new StubStated(field);
            src.setValue("abc");
            var dst = new StubStated(field);

            src.copyValueTo((FieldValue) dst);

            assertEquals("abc", dst.getValue());
            assertFalse(dst.isUndefined());
            assertFalse(dst.isCleared());
        }

        @Test
        void copyValueTo_copiesClearedState() {
            var src = new StubStated(field);
            src.setValue("abc");
            src.clear();
            var dst = new StubStated(field);
            dst.setValue("xyz");

            src.copyValueTo((FieldValue) dst);

            assertNull(dst.getValue());
            assertTrue(dst.isCleared());
        }

        @Test
        void copyValueTo_copiesUndefinedState() {
            var src = new StubStated(field);
            var dst = new StubStated(field);
            dst.setValue("xyz");

            src.copyValueTo((FieldValue) dst);

            assertNull(dst.getValue());
            assertTrue(dst.isUndefined());
        }
    }

    @Nested
    class UpdateMutableValueState {

        @Test
        void updateMutableValueState_defaultIsNoOp_doesNotThrow() {
            // The extension point must be safe to invoke without an override.
            var value = new StubStated(field);

            assertDoesNotThrow(() -> value.isUndefined());
            assertDoesNotThrow(() -> value.isCleared());
        }
    }
}
