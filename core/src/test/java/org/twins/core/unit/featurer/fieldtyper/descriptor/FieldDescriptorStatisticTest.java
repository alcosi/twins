package org.twins.core.unit.featurer.fieldtyper.descriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorStatistic;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldDescriptorStatisticTest extends BaseUnitTest {

    private FieldDescriptorStatistic descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new FieldDescriptorStatistic();
    }

    @Nested
    class Defaults {

        @Test
        void twinStatisticId_defaultsToNull() {
            assertNull(descriptor.getTwinStatisticId());
        }
    }

    @Nested
    class Accessors {

        @Test
        void chainedAccessor_roundTripValueAndReturnsSelf() {
            // FieldDescriptorStatistic uses @Accessors(chain = true), not fluent
            var id = UUID.randomUUID();
            var returned = descriptor.setTwinStatisticId(id);

            assertSame(descriptor, returned, "chain accessor must return self");
            assertEquals(id, descriptor.getTwinStatisticId());
        }
    }
}
