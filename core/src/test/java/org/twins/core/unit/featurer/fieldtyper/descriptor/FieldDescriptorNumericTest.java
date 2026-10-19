package org.twins.core.unit.featurer.fieldtyper.descriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;

import static org.junit.jupiter.api.Assertions.*;

class FieldDescriptorNumericTest extends BaseUnitTest {

    private FieldDescriptorNumeric descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new FieldDescriptorNumeric();
    }

    @Nested
    class Defaults {

        @Test
        void allOptionalAttributes_defaultToNull() {
            // Optional descriptor attributes must default to null (unspecified), not a sentinel value
            assertNull(descriptor.min());
            assertNull(descriptor.max());
            assertNull(descriptor.step());
            assertNull(descriptor.thousandSeparator());
            assertNull(descriptor.decimalSeparator());
            assertNull(descriptor.decimalPlaces());
            assertNull(descriptor.extraThousandSeparators());
            assertNull(descriptor.extraDecimalSeparators());
            assertNull(descriptor.round());
        }
    }

    @Nested
    class Accessors {

        @Test
        void fluentAccessors_roundTripValues() {
            descriptor.min(-5.0);
            descriptor.max(5.0);
            descriptor.step(0.5);
            descriptor.decimalPlaces(2);
            descriptor.round(true);

            assertEquals(-5.0, descriptor.min());
            assertEquals(5.0, descriptor.max());
            assertEquals(0.5, descriptor.step());
            assertEquals(2, descriptor.decimalPlaces());
            assertTrue(descriptor.round());
        }

        @Test
        void fluentAccessors_returnSelfForChaining() {
            var returned = descriptor.min(1.0).max(2.0);
            assertSame(descriptor, returned);
        }
    }

    @Nested
    class Equality {

        @Test
        void equals_sameConstraints_areEqual() {
            var a = new FieldDescriptorNumeric();
            a.min(1.0);
            a.max(10.0);

            var b = new FieldDescriptorNumeric();
            b.min(1.0);
            b.max(10.0);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        void equals_differentMin_areNotEqual() {
            var a = new FieldDescriptorNumeric();
            a.min(1.0);

            var b = new FieldDescriptorNumeric();
            b.min(2.0);

            assertNotEquals(a, b);
        }
    }
}
