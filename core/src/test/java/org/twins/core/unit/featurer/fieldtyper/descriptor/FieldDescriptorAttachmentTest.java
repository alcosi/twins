package org.twins.core.unit.featurer.fieldtyper.descriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorAttachment;

import static org.junit.jupiter.api.Assertions.*;

class FieldDescriptorAttachmentTest extends BaseUnitTest {

    private FieldDescriptorAttachment descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new FieldDescriptorAttachment();
    }

    @Nested
    class Defaults {

        @Test
        void optionalAttributes_defaultToNull() {
            assertNull(descriptor.minCount());
            assertNull(descriptor.maxCount());
            assertNull(descriptor.extensions());
            assertNull(descriptor.filenameRegExp());
            assertNull(descriptor.fileSizeMbLimit());
        }
    }

    @Nested
    class Accessors {

        @Test
        void fluentAccessors_roundTripValues() {
            descriptor.minCount(1);
            descriptor.maxCount(5);
            descriptor.extensions("png,jpg");
            descriptor.filenameRegExp("^[a-z]+$");
            descriptor.fileSizeMbLimit(10);

            assertEquals(1, descriptor.minCount());
            assertEquals(5, descriptor.maxCount());
            assertEquals("png,jpg", descriptor.extensions());
            assertEquals("^[a-z]+$", descriptor.filenameRegExp());
            assertEquals(10, descriptor.fileSizeMbLimit());
        }
    }
}
