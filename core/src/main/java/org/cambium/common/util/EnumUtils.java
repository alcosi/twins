package org.cambium.common.util;


import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static <T extends Enum<T>> Set<String> convertOrNull(Set<T> collection) {
        return collection == null ? Collections.emptySet() : collection.stream().map(Enum::name).collect(Collectors.toSet());
    }
}
