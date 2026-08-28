package org.twins.core.unit.featurer.fieldtyper.descriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.enums.twinclass.FieldCheckboxType;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorBoolean;

import static org.junit.jupiter.api.Assertions.*;

class FieldDescriptorBooleanTest extends BaseUnitTest {

    private FieldDescriptorBoolean descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new FieldDescriptorBoolean();
    }

    @Nested
    class Defaults {

        @Test
        void optionalAttributes_defaultToNull() {
            assertNull(descriptor.checkboxType());
            assertNull(descriptor.nullable());
        }
    }

    @Nested
    class Accessors {

        @Test
        void fluentAccessors_roundTripValues() {
            descriptor.checkboxType(FieldCheckboxType.TOGGLE);
            descriptor.nullable(true);

            assertSame(FieldCheckboxType.TOGGLE, descriptor.checkboxType());
            assertTrue(descriptor.nullable());
        }
    }
}
