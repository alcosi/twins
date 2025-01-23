package org.cambium.common.util;


import java.util.Arrays;

public class ArrayUtils {
    @SafeVarargs
    public static <T> T[] concatArray(T[] array, T... elements) {
        T[] result = Arrays.copyOf(array, array.length + elements.length);
        System.arraycopy(elements, 0, result, array.length, elements.length);
        return result;
    }

    public static <T> T[] concatArray(T element,T[] array) {
        T[] result = Arrays.copyOf(array, array.length + 1);
        result[array.length] = element;
        return result;
    }
}
