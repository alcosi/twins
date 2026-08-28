package org.twins.core.featurer.fieldrule.fieldoverwriter;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.featurer.fieldrule.fieldoverwriter.FieldParamOverwriterNumeric;

import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;


class FieldParamOverwriterNumericTest extends BaseUnitTest {

    private final FieldParamOverwriterNumeric overwriter = new FieldParamOverwriterNumeric();
    private TwinClassFieldRuleEntity rule;

    @BeforeEach
    void setUp() {
        rule = new TwinClassFieldRuleEntity();
    }

    private Properties props(String min, String max, String step, String thousandSeparator, String decimalSeparator, String decimalPlaces) {
        var props = new Properties();
        props.put("min", min != null ? min : "");
        props.put("max", max != null ? max : "");
        props.put("step", step != null ? step : "");
        if (thousandSeparator != null)
            props.put("thousandSeparator", thousandSeparator);
        if (decimalSeparator != null)
            props.put("decimalSeparator", decimalSeparator);
        if (decimalPlaces != null)
            props.put("decimalPlaces", decimalPlaces);

        return props;
    }

    @Nested
    class GetFieldOverwriterDescriptor {

        @Test
        void getFieldOverwriterDescriptor_allParamsSet_returnsFullyPopulatedDescriptor() throws ServiceException {
            var descriptor = overwriter.getFieldOverwriterDescriptor(
                    rule,
                    props("0.5", "100.0", "0.1", " ", ".", "2")
            );

            assertEquals(0.5, descriptor.min());
            assertEquals(100.0, descriptor.max());
            assertEquals(0.1, descriptor.step());
            assertEquals(" ", descriptor.thousandSeparator());
            assertEquals(".", descriptor.decimalSeparator());
            assertEquals(2, descriptor.decimalPlaces());
        }

        @Test
        void getFieldOverwriterDescriptor_emptyDoubleParams_returnsNullDoubles() throws ServiceException {
            var descriptor = overwriter.getFieldOverwriterDescriptor(rule, props(null, null, null, null, null, null));

            assertNull(descriptor.min());
            assertNull(descriptor.max());
            assertNull(descriptor.step());
            assertNull(descriptor.thousandSeparator());
            assertNull(descriptor.decimalSeparator());
            assertNull(descriptor.decimalPlaces());
        }

        @Test
        void getFieldOverwriterDescriptor_negativeBounds_preservedInDescriptor() throws ServiceException {
            var descriptor = overwriter.getFieldOverwriterDescriptor(
                    rule,
                    props("-50.0", "-10.0", "0.5", null, null, null)
            );

            assertEquals(-50.0, descriptor.min());
            assertEquals(-10.0, descriptor.max());
        }

        @Test
        void getFieldOverwriterDescriptor_zeroDecimalPlaces_returnsZero() throws ServiceException {
            var descriptor = overwriter.getFieldOverwriterDescriptor(rule, props("0", "1", "1", null, null, "0"));

            assertEquals(0, descriptor.decimalPlaces());
        }
    }
}
