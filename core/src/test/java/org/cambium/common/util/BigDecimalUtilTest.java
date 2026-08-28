package org.cambium.common.util;

import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BigDecimalUtilTest extends BaseUnitTest {

    @Test
    void getProcessedString_integerValue_returnsIntegerString() {
        assertEquals("12", BigDecimalUtil.getProcessedString(new BigDecimal("12.000000")));
    }

    @Test
    void getProcessedString_integerValueWithNonZeroFraction_doesNotReturnInteger() {
        assertNotEquals("12", BigDecimalUtil.getProcessedString(new BigDecimal("12.000010")));
    }

    @Test
    void getProcessedString_integerValue_doesNotMatchDecimalString() {
        assertNotEquals("12.34234", BigDecimalUtil.getProcessedString(new BigDecimal("12.000000")));
    }

    @Test
    void getProcessedString_decimalValue_returnsDecimalString() {
        assertEquals("12.04245", BigDecimalUtil.getProcessedString(new BigDecimal("12.04245")));
    }

    @Test
    void getProcessedString_zeroWithFraction_doesNotMatchOtherDecimal() {
        assertNotEquals("0.34234", BigDecimalUtil.getProcessedString(new BigDecimal("0.000000")));
    }

    @Test
    void getProcessedString_zeroWithNonZeroFraction_returnsDecimalString() {
        assertEquals("0.04245", BigDecimalUtil.getProcessedString(new BigDecimal("0.04245")));
    }
}
