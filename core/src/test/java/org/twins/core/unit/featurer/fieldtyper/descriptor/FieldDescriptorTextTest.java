package org.twins.core.unit.featurer.fieldtyper.descriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.enums.twinclass.FieldTextEditorType;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;

import static org.junit.jupiter.api.Assertions.*;

class FieldDescriptorTextTest extends BaseUnitTest {

    private FieldDescriptorText descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new FieldDescriptorText();
    }

    @Nested
    class Defaults {

        @Test
        void optionalAttributes_defaultToNull() {
            assertNull(descriptor.regExp());
            assertNull(descriptor.editorType());
        }
    }

    @Nested
    class Accessors {

        @Test
        void fluentAccessors_roundTripValues() {
            descriptor.regExp("^[a-z]+$");
            descriptor.editorType(FieldTextEditorType.HTML);

            assertEquals("^[a-z]+$", descriptor.regExp());
            assertSame(FieldTextEditorType.HTML, descriptor.editorType());
        }
    }

    @Nested
    class Equality {

        @Test
        void equals_sameRegExpAndEditorType_areEqual() {
            var a = new FieldDescriptorText();
            a.regExp("x");
            a.editorType(FieldTextEditorType.PLAIN);

            var b = new FieldDescriptorText();
            b.regExp("x");
            b.editorType(FieldTextEditorType.PLAIN);

            assertEquals(a, b);
        }
    }
}
