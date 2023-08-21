package org.cambium.common.util;

import java.util.Arrays;
import java.util.Comparator;

public class EnumUtils {
    public static <T extends Enum<T>> T[] sortedValues(Class<T> enumClass) {
        T[] ret = enumClass.getEnumConstants();
        Arrays.sort(ret, EnumByNameComparator.INSTANCE);
        return ret;
    }

    private static class EnumByNameComparator implements Comparator<Enum<?>> {
        public static final Comparator<Enum<?>> INSTANCE = new EnumByNameComparator();

        public int compare(Enum<?> enum1, Enum<?> enum2) {
            return enum1.name().compareTo(enum2.name());
        }

    }
}
