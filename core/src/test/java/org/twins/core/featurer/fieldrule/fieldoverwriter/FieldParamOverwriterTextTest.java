package org.twins.core.featurer.fieldrule.fieldoverwriter;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.enums.twinclass.FieldTextEditorType;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class FieldParamOverwriterTextTest extends BaseUnitTest {

    private final FieldParamOverwriterText overwriter = new FieldParamOverwriterText();
    private TwinClassFieldRuleEntity rule;

    @BeforeEach
    void setUp() {
        rule = new TwinClassFieldRuleEntity();
    }

    private Properties props(String regexp, String editorType) {
        var props = new Properties();
        if (regexp != null)
            props.put("regexp", regexp);
        if (editorType != null)
            props.put("editorType", editorType);

        return props;
    }

    @Nested
    class GetFieldOverwriterDescriptor {

        @Test
        void getFieldOverwriterDescriptor_regexpAndEditorTypeSet_returnsDescriptorWithBoth() throws ServiceException {
            var descriptor = overwriter.getFieldOverwriterDescriptor(rule, props("[A-Z]+", "MARKDOWN_GITHUB"));

            assertEquals("[A-Z]+", descriptor.regExp());
            assertEquals(FieldTextEditorType.MARKDOWN_GITHUB, descriptor.editorType());
        }

        @Test
        void getFieldOverwriterDescriptor_editorTypeMissing_defaultsToPlain() throws ServiceException {
            var descriptor = overwriter.getFieldOverwriterDescriptor(rule, props("[A-Z]+", null));

            assertEquals(FieldTextEditorType.PLAIN, descriptor.editorType());
        }

        @Test
        void getFieldOverwriterDescriptor_regexpMissing_returnsNullRegexp() throws ServiceException {
            var descriptor = overwriter.getFieldOverwriterDescriptor(rule, props(null, "HTML"));

            assertNull(descriptor.regExp());
            assertEquals(FieldTextEditorType.HTML, descriptor.editorType());
        }

        @Test
        void getFieldOverwriterDescriptor_emptyProperties_returnsDescriptorWithDefaults() throws ServiceException {
            var descriptor = overwriter.getFieldOverwriterDescriptor(rule, new Properties());

            assertNull(descriptor.regExp());
            assertEquals(FieldTextEditorType.PLAIN, descriptor.editorType());
        }
    }
}
