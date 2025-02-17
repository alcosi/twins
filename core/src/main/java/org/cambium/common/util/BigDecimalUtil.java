package org.cambium.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class providing convenience methods for working with BigDecimal values.
 * This class is designed to handle common operations related to BigDecimal processing.
 */
public class BigDecimalUtil {

    /**
     * Converts a BigDecimal value into a string representation based on its scale.
     * If the value has no fractional part after rounding to the nearest integer,
     * it is represented without decimal places.
     * Otherwise, the original value's string representation is returned.
     *
     * @param difference the BigDecimal value to be converted to a string.
     * @return the string representation of the BigDecimal value, scaled appropriately.
     */
    public static String getProcessedString(BigDecimal difference) {
        BigDecimal scaled = difference.setScale(0, RoundingMode.HALF_UP);
        if (difference.compareTo(scaled) == 0) {
            return scaled.toString();
        }
        return difference.toString();
    }
}
