package org.cambium.common.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
    public static <T> List<List<T>> partition(List<T> list, int size) {
        if (size <= 0 || list.isEmpty()) {
            return List.of(list);
        }
        if (size >= list.size()) {
            return List.of(list);
        }
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
        }
        return result;
    }
}
