package org.cambium.common.util;

import org.apache.commons.collections.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

public class SetUtils {
    public static <T> Set<T> narrowSet(Set<T> mainSet, Set<T> narrowSet) {
        if (org.apache.commons.collections.CollectionUtils.isEmpty(narrowSet))
            return mainSet;
        if (CollectionUtils.isEmpty(mainSet)) //it is ok to limit search in this case
            return narrowSet;
        if (mainSet.size() == 1) // not possible to narrow
            return mainSet;
        Set<T> crossSet = new HashSet<>();
        for (T narrowId : narrowSet) {
            if (mainSet.contains(narrowId))  //we have crossing
                crossSet.add(narrowId);
            // else element must be skipped, because it is not in original set
        }
        return crossSet.isEmpty() ? mainSet : crossSet;
    }

    public static <T> Set<T>  safeAdd(Set<T> set, T element) {
        if (set != null && element != null) set.add(element);
        return set;
    }
}
