package org.cambium.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;


/**
 * Test class for validating the methods in BigDecimalUtil.
 * This class contains unit tests for the BigDecimalUtil.getProcessedString method.
 * The tests validate different scenarios of processing BigDecimal values to their expected
 * string representations, ensuring the method behaves as intended.
 */
class BigDecimalUtilTest {


    /**
     * Verifies that the BigDecimalUtil.getProcessedString method correctly processes
     * a BigDecimal with an integer value and its string representation matches
     * the expected output when no fractional part is present.
     * This test case uses an integer BigDecimal value (e.g., "12.000000"),
     * validates that it is processed into its integer string equivalent,
     * and ensures no extraneous decimal points or zeros are retained in the result.
     * Assertions:
     * - Ensures that the method returns the expected string representation
     *   of the BigDecimal value without fractional parts when valid.
     */
    @Test
    void getProcessedStringForIntegerValid() {
        String expected = "12";
        BigDecimal value = new BigDecimal("12.000000");
        String result = BigDecimalUtil.getProcessedString(value);
        Assertions.assertEquals(expected, result);
    }

    /**
     * Validates the output of the BigDecimalUtil.getProcessedString(BigDecimal) method when the input
     * BigDecimal value has non-zero fractional parts that exceed the threshold for rounding to an
     * integer string representation.
     * The test ensures that the result of processing such BigDecimal values does not match the expected
     * integer string representation, as the original value has a precision discrepancy compared to the expected.
     * Test objective:
     * - Verifies that the processing of BigDecimal values with minor non-zero fractional parts
     *   does not incorrectly match the expected integer output.
     * Assertions:
     * - Compares the actual result of processing the BigDecimal value with an incorrect expected value
     *   to validate that they do not match.
     */
    @Test
    void getProcessedStringForIntegerVotValid() {
        String expected = "12";
        BigDecimal value = new BigDecimal("12.000010");
        String result = BigDecimalUtil.getProcessedString(value);
        Assertions.assertNotEquals(expected, result);
    }

    /**
     * Validates the string representation of a BigDecimal value for a case
     * where a double precision value is expected, but the input value does not
     * match the desired string representation. Specifically, compares the result
     * of processing the BigDecimal to a given expected string and asserts that
     * the values are not equal.
     * The test is designed to ensure that the BigDecimalUtil.getProcessedString()
     * method does not incorrectly format a value meant to differ from the
     * specified expected string.
     * Test Scenario:
     * - The BigDecimal value is "12.000000".
     * - The expected result string is "12.34234".
     * - The assertion checks that the processed result and the expected string
     *   differ.
     */
    @Test
    void getProcessedStringForDoubleVotValid() {
        String expected = "12.34234";
        BigDecimal value = new BigDecimal("12.000000");
        String result = BigDecimalUtil.getProcessedString(value);
        Assertions.assertNotEquals(expected, result);
    }

    /**
     * Tests the {@link BigDecimalUtil#getProcessedString(BigDecimal)} method's ability
     * to correctly process a valid BigDecimal value with fractional part.
     * This test ensures that the method converts a BigDecimal with a non-zero fractional
     * portion into its expected string representation, preserving the fractional digits
     * accurately. The returned string should match the expected value.
     * Assertions:
     * - The result string must match the expected string representation of the BigDecimal value.
     */
    @Test
    void getProcessedStringForDoubleValid() {
        String expected = "12.04245";
        BigDecimal value = new BigDecimal("12.04245");
        String result = BigDecimalUtil.getProcessedString(value);
        Assertions.assertEquals(expected, result);
    }

    /**
     * Tests the behavior of the BigDecimalUtil.getProcessedString method with a BigDecimal
     * value starting with zeros and an expected string representation that does not match
     * the result. Verifies that the processed string does not equal the provided expected value.
     * The test ensures that the method correctly processes values with initial zeros,
     * reflecting the actual structure of the input value while not necessarily matching
     * the arbitrary expected string.
     * Preconditions:
     * - The BigDecimal value is initialized with a string containing leading zeros ("0.000000").
     * Assertions:
     * - Asserts that the processed string representation of the BigDecimal value does not
     *   match the given expected string "0.34234".
     */
    @Test
    void getProcessedStringForDoubleZeroStartVotValid() {
        String expected = "0.34234";
        BigDecimal value = new BigDecimal("0.000000");
        String result = BigDecimalUtil.getProcessedString(value);
        Assertions.assertNotEquals(expected, result);
    }

    /**
     * Verifies that the method BigDecimalUtil.getProcessedString correctly processes
     * a BigDecimal value starting with a double zero and containing non-zero fractional
     * parts. The expected result should be a string representation of the value, matching
     * the non-zero fractional part.
     * The test ensures:
     * - Proper handling of BigDecimal values starting with "0."
     * - Accuracy in generating string representation without unnecessary modifications.
     * It asserts that the returned string from getProcessedString matches the expected
     * string representation of the input BigDecimal value.
     */
    @Test
    void getProcessedStringForDoubleZeroStartValid() {
        String expected = "0.04245";
        BigDecimal value = new BigDecimal(expected);
        String result = BigDecimalUtil.getProcessedString(value);
        Assertions.assertEquals(expected, result);
    }
}
