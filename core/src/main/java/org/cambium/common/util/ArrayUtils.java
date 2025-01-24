package org.cambium.common.util;


import java.util.Arrays;

public class ArrayUtils {
    @SafeVarargs
    public static <T> T[] concatArray(T[] array, T... elements) {
        T[] result = Arrays.copyOf(array, array.length + elements.length);
        System.arraycopy(elements, 0, result, array.length, elements.length);
        return result;
    }

    public static <T> T[] concatArray(T element, T... elements) {
        T[] result = (T[]) java.lang.reflect.Array.newInstance(element.getClass(), elements.length + 1);
        System.arraycopy(elements, 0, result, 1, elements.length);
        result[0] = element;
        return result;
    }

}
