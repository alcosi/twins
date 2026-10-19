package org.twins.core.unit.featurer.fieldtyper.descriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorListShared;

import static org.junit.jupiter.api.Assertions.*;

class FieldDescriptorListSharedTest extends BaseUnitTest {

    private FieldDescriptorListShared descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new FieldDescriptorListShared();
    }

    @Nested
    class Defaults {

        @Test
        void multiple_defaultsToFalse() {
            assertFalse(descriptor.isMultiple());
        }
    }

    @Nested
    class Accessors {

        @Test
        void chainedAccessor_roundTripValueAndReturnsSelf() {
            // FieldDescriptorListShared uses @Accessors(chain = true) -> setter-style accessor
            var returned = descriptor.setMultiple(true);

            assertSame(descriptor, returned, "chain accessor must return self");
            assertTrue(descriptor.isMultiple());
        }
    }
}
