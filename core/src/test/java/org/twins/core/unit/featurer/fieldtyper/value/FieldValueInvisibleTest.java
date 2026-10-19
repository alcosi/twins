package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueInvisible;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldValueInvisible is the "no value here" terminal of the hierarchy.
 * Intended contract:
 *   - isUndefined() is always true
 *   - isCleared()   is always false
 *   - hasValue()    is always false
 *   - copyValueTo() is a no-op
 *   - undefine()    returns this
 *   - clear()       returns this (chaining), see RED test below
 */
class FieldValueInvisibleTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    @Nested
    class TerminalState {

        @Test
        void isUndefined_alwaysTrue() {
            var value = new FieldValueInvisible(field);

            assertTrue(value.isUndefined());
        }

        @Test
        void isCleared_alwaysFalse() {
            var value = new FieldValueInvisible(field);

            assertFalse(value.isCleared());
        }

        @Test
        void isEmpty_alwaysTrue_becauseUndefined() {
            var value = new FieldValueInvisible(field);

            assertTrue(value.isEmpty());
            assertFalse(value.isNotEmpty());
            assertFalse(value.isDefined());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_anyInput_returnsFalse() {
            var value = new FieldValueInvisible(field);

            assertFalse(value.hasValue("anything"));
            assertFalse(value.hasValue(null));
        }
    }

    @Nested
    class Operations {

        @Test
        void undefine_returnsThis() {
            var value = new FieldValueInvisible(field);

            assertSame(value, value.undefine());
        }

        @Test
        void copyValueTo_isNoOpAndDoesNotThrow() {
            var value = new FieldValueInvisible(field);
            var dst = new FieldValueInvisible(field);

            assertDoesNotThrow(() -> value.copyValueTo(dst));
        }
    }

    @Nested
    class Clear {

        // =====================================================================
        // RED — encodes PROD BUG (sibling of #2): FieldValueInvisible.clear()
        //   returns `null` instead of `this`, breaking the chain contract shared
        //   by FieldValue.undefine()/FieldValueStated.undefine()/etc.
        // Intended contract: clear() returns the value for chaining, like undefine().
        // =====================================================================
        @Test
        void clear_returnsThisForChaining() {
            var value = new FieldValueInvisible(field);

            var returned = value.clear();

            assertSame(value, returned);
        }
    }

    @Nested
    class NewInstance {

        @Test
        void newInstance_yieldsFreshInvisible() {
            var src = new FieldValueInvisible(field);

            var created = src.newInstance(field);

            assertInstanceOf(FieldValueInvisible.class, created);
            assertNotSame(src, created);
            assertTrue(created.isUndefined());
        }
    }
}
