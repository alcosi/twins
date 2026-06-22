package org.twins.core.unit.featurer.fieldtyper.descriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorDate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FieldDescriptorDateTest extends BaseUnitTest {

    private FieldDescriptorDate descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new FieldDescriptorDate();
    }

    @Nested
    class Defaults {

        @Test
        void optionalAttributes_defaultToNull() {
            assertNull(descriptor.pattern());
            assertNull(descriptor.beforeDate());
            assertNull(descriptor.afterDate());
        }
    }

    @Nested
    class Accessors {

        @Test
        void fluentAccessors_roundTripValues() {
            var before = LocalDateTime.of(2026, 1, 1, 0, 0);
            var after = LocalDateTime.of(2020, 1, 1, 0, 0);

            descriptor.pattern("yyyy-MM-dd");
            descriptor.beforeDate(before);
            descriptor.afterDate(after);

            assertEquals("yyyy-MM-dd", descriptor.pattern());
            assertEquals(before, descriptor.beforeDate());
            assertEquals(after, descriptor.afterDate());
        }
    }
}
