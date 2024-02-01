package org.cambium.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionUtils extends org.apache.commons.collections.CollectionUtils {
    public static List<String> singletonListOrNull(String string) {
        if (StringUtils.isNotBlank(string))
            return Collections.singletonList(string);
        else
            return null;
    }

    public static <T> List<T> safeAdd(List<T> list, T element) {
        if (list == null)
            list = new ArrayList<>();
        list.add(element);
        return list;
    }

    public static <T> List<T> safeAdd(List<T> list, Collection<T> elements) {
        if (list == null)
            list = new ArrayList<>();
        list.addAll(elements);
        return list;
    }

    public static <T> Collection<T> safeAdd(Collection<T> list, T element) {
        if (list == null)
            list = new ArrayList<>();
        list.add(element);
        return list;
    }

    public static <T> Collection<T> safeAdd(Collection<T> list, Collection<T> elements) {
        if (list == null)
            list = new ArrayList<>();
        list.addAll(elements);
        return list;
    }
}
