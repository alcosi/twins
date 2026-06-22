package org.twins.core.unit.featurer.fieldtyper.descriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorSecret;

import static org.junit.jupiter.api.Assertions.*;

class FieldDescriptorSecretTest extends BaseUnitTest {

    private FieldDescriptorSecret descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new FieldDescriptorSecret();
    }

    @Nested
    class Defaults {

        @Test
        void regExp_defaultsToNull() {
            assertNull(descriptor.regExp());
        }
    }

    @Nested
    class Accessors {

        @Test
        void fluentAccessor_roundTripValue() {
            descriptor.regExp("^.+$");
            assertEquals("^.+$", descriptor.regExp());
        }
    }
}
