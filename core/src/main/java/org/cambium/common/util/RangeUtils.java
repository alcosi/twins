package org.cambium.common.util;


import org.cambium.common.math.IntegerRange;

public class RangeUtils {

    public static boolean isEmpty(IntegerRange range) {
        if (range == null) return true;
        return range.getFrom() == null && range.getTo() == null;
    }

}
