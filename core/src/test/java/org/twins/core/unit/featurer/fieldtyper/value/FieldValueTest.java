package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.cambium.common.ValidationResult;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.enums.consts.SystemIds;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldValue is the abstract root of the field-value hierarchy. It carries the
 * twinClassField reference, the validation/systemInitialized flags, and the
 * template-method skeleton (clone / copyValueTo / newInstance / isEmpty family).
 *
 * Concrete behavior of the abstract methods is exercised by the subclasses' tests;
 * here we lock down the contract owned by FieldValue itself.
 */
class FieldValueTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    /**
     * Minimal concrete subclass so we can drive the template methods without
     * coupling the test to any particular production subclass semantics.
     * State is fully controlled inside the stub: PRESENT when set, CLEARED on clear.
     */
    static class StubFieldValue extends FieldValue {
        private String value;
        private boolean undefined = true;

        StubFieldValue(TwinClassFieldEntity twinClassField) {
            super(twinClassField);
        }

        @Override
        public FieldValue newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
            return new StubFieldValue(newTwinClassFieldEntity);
        }

        @Override
        public boolean hasValue(String value) {
            return java.util.Objects.equals(this.value, value);
        }

        @Override
        public void copyValueTo(FieldValue dst) {
            ((StubFieldValue) dst).value = this.value;
        }

        @Override
        public FieldValue undefine() {
            value = null;
            undefined = true;
            return this;
        }

        @Override
        public boolean isUndefined() {
            return undefined;
        }

        @Override
        public FieldValue clear() {
            value = null;
            undefined = false; // cleared is "defined-but-empty"
            return this;
        }

        @Override
        public boolean isCleared() {
            return !undefined && value == null;
        }

        void setValue(String v) {
            this.value = v;
            this.undefined = false;
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
    class Construction {

        @Test
        void constructor_storesTwinClassField() {
            var value = new StubFieldValue(field);

            assertSame(field, value.getTwinClassField());
        }

        @Test
        void getTwinClassFieldId_delegatesToField() {
            var id = UUID.randomUUID();
            field.setId(id);

            var value = new StubFieldValue(field);

            assertEquals(id, value.getTwinClassFieldId());
        }

        @Test
        void isBaseField_delegatesToField() {
            // isBaseField() derives from the field id via SystemIdLookup.isSystemField(id);
            // a system field id (e.g. TWIN_NAME) makes the field a base field.
            field.setId(SystemIds.TwinClassField.Base.NAME);

            var value = new StubFieldValue(field);

            assertTrue(value.isBaseField());
        }

        @Test
        void systemInitialized_defaultsToFalse() {
            var value = new StubFieldValue(field);

            assertFalse(value.isSystemInitialized());
        }

        @Test
        void setSystemInitialized_isChained() {
            var value = new StubFieldValue(field);

            var returned = value.setSystemInitialized(true);

            assertSame(value, returned);
            assertTrue(value.isSystemInitialized());
        }
    }

    @Nested
    class Validation {

        @Test
        void isValidated_nullResult_returnsFalse() {
            var value = new StubFieldValue(field);

            assertFalse(value.isValidated());
        }

        @Test
        void isValidated_withResult_returnsTrue() {
            var value = new StubFieldValue(field);
            value.setValidationResult(new ValidationResult(true));

            assertTrue(value.isValidated());
        }

        @Test
        void setValidationResult_returnsThisForChaining() {
            var value = new StubFieldValue(field);
            var result = new ValidationResult(false);

            var returned = value.setValidationResult(result);

            // Lombok @Accessors(chain=true) setter returns this (the FieldValue), not the stored result.
            assertSame(value, returned);
            assertSame(result, value.getValidationResult());
        }

        @Test
        void initValidationResult_storesAndReturnsSameReference() {
            var value = new StubFieldValue(field);
            var result = new ValidationResult(true);

            var returned = value.initValidationResult(result);

            assertSame(result, returned);
            assertSame(result, value.getValidationResult());
        }
    }

    @Nested
    class EmptyFamily {

        @Test
        void isUndefined_whenUndefined_isEmptyTrueIsDefinedFalse() {
            var value = new StubFieldValue(field);

            assertTrue(value.isUndefined());
            assertTrue(value.isEmpty());
            assertFalse(value.isDefined());
            assertFalse(value.isNotEmpty());
        }

        @Test
        void isEmpty_whenCleared_returnsTrue() {
            var value = new StubFieldValue(field);
            value.clear();

            assertTrue(value.isCleared());
            assertTrue(value.isEmpty());
            assertTrue(value.isDefined());
            assertFalse(value.isNotEmpty());
        }

        @Test
        void isNotEmpty_whenValuePresent_returnsTrue() {
            var value = new StubFieldValue(field);
            value.setValue("x");

            assertFalse(value.isEmpty());
            assertTrue(value.isNotEmpty());
            assertTrue(value.isDefined());
            assertFalse(value.isUndefined());
        }
    }

    @Nested
    class Clone {

        @Test
        void clone_usesSameTwinClassFieldAndCopiesValue() {
            var original = new StubFieldValue(field);
            original.setValue("abc");

            var clone = original.clone();

            assertNotSame(original, clone);
            assertSame(field, clone.getTwinClassField());
            assertEquals("abc", ((StubFieldValue) clone).getValue());
        }

        @Test
        void clone_withNewField_swapsTwinClassField() {
            var original = new StubFieldValue(field);
            original.setValue("abc");
            var newField = new TwinClassFieldEntity();

            var clone = original.clone(newField);

            assertSame(newField, clone.getTwinClassField());
            assertEquals("abc", ((StubFieldValue) clone).getValue());
        }

        // =====================================================================
        // RED — encodes PROD BUG #1 documented in the audit:
        //   FieldValue has no @EqualsAndHashCode, yet every subclass declares
        //   @EqualsAndHashCode(callSuper = true). That delegates equals() up to
        //   FieldValue, which inherits Object.equals() (identity). Two distinct
        //   FieldValue instances are therefore never equal, so clone()/copyValueTo()
        //   results never compare equal to their source.
        // Intended contract: a clone of a value is equal-by-value to the original.
        // =====================================================================
        @Test
        @Disabled("bug #1: base FieldValue classes lack @EqualsAndHashCode -> equals is identity, so clone never equals original.")
        void clone_isEqualToOriginalByValue() {
            var original = new StubFieldValue(field);
            original.setValue("abc");

            var clone = original.clone();

            assertEquals(original, clone);
        }
    }
}
