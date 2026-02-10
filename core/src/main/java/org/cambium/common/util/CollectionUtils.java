package org.cambium.common.util;

import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionUtils extends org.apache.commons.collections.CollectionUtils {

    public static <T> String generateUniqueKey(Collection<T> collection) {
        return collection.stream()
                .sorted()
                .map(T::toString)
                .collect(Collectors.joining(","));
    }

    public static List<String> singletonListOrNull(String string) {
        if (StringUtils.isNotBlank(string))
            return Collections.singletonList(string);
        else
            return null;
    }

    public static <T> List<T> safeAdd(List<T> list, T element) {
        if (element == null && list == null)
            return null;
        if (list == null)
            list = new ArrayList<>();
        if (element != null)
            list.add(element);
        return list;
    }

    public static <T> List<T> safeAdd(List<T> list, Collection<T> elements) {
        if (isEmpty(elements) && list == null)
            return null;
        if (list == null)
            list = new ArrayList<>();
        if (elements != null)
            list.addAll(elements);
        return list;
    }

    public static <T> Set<T> safeAdd(Set<T> set, T element) {
        if (element == null && set == null)
            return null;
        if (set == null)
            set = new HashSet<>();
        if (element != null)
            set.add(element);
        return set;
    }

    public static <T> Set<T> safeAdd(Set<T> set, Collection<T> elements) {
        if (isEmpty(elements) && set == null)
            return null;
        if (set == null)
            set = new HashSet<>();
        if (elements != null)
            set.addAll(elements);
        return set;
    }

    public static <T> Collection<T> safeAdd(Collection<T> list, T element) {
        if (element == null && list == null)
            return null;
        if (list == null)
            list = new ArrayList<>();
        if (element != null)
            list.add(element);
        return list;
    }

    public static <T> Collection<T> safeAdd(Collection<T> list, Collection<T> elements) {
        if (isEmpty(elements) && list == null)
            return null;
        if (list == null)
            list = new ArrayList<>();
        if (elements != null)
            list.addAll(elements);
        return list;
    }

    public static <T> Set<T> convertToSetSafe(Collection<T> collection) {
        if (collection == null)
            return null;
        return new HashSet<>(collection);
    }

    public static boolean isEmpty(Map coll) {
        return coll == null ||
                coll.isEmpty();
    }

    @SafeVarargs
    public static <T> Set<T> getFirstNotEmpty(Set<T>... collections) {
         for (Set<T> collection : collections) {
             if (collection != null && !collection.isEmpty())
                 return collection;
         }
         return null;
    }
}
