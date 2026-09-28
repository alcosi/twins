package org.twins.core.featurer.fieldtyper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FieldValueChangeHelper {
    public static boolean isSingleToSingleValueUpdate(List<?> newValueList, Map<UUID, ?> oldValueMap) {
        return newValueList != null && newValueList.size() == 1 && oldValueMap != null && oldValueMap.size() == 1;
    }

    public static boolean isAnyToSingleValueUpdate(List<?> newValueList, Map<UUID, ?> oldValueMap) {
        return newValueList != null && newValueList.size() == 1 && oldValueMap != null && oldValueMap.size() >= 1;
    }

    public static boolean isSingleValueAdd(List<?> newValueList, Map<UUID, ?> oldValueMap) {
        return newValueList != null && newValueList.size() == 1 && MapUtils.isEmpty(oldValueMap);
    }

    public static boolean notSaved(UUID valueId, Map<UUID, ?> oldValueMap) {
        return oldValueMap == null || !oldValueMap.containsKey(valueId);
    }

    public static boolean hasOutOfDateValues(Map<UUID, ?> oldValueMap) {
        return oldValueMap != null && CollectionUtils.isNotEmpty(oldValueMap.entrySet());
    }

    /**
     * Same ids with the same multiplicity. A null id is not comparable and yields false.
     */
    public static boolean sameIdMultiset(Collection<UUID> left, Collection<UUID> right) {
        if (left == null || right == null || left.size() != right.size())
            return false;
        Map<UUID, Integer> counts = new HashMap<>();
        for (UUID id : left) {
            if (id == null)
                return false;
            counts.merge(id, 1, Integer::sum);
        }
        for (UUID id : right) {
            Integer count = id == null ? null : counts.get(id);
            if (count == null)
                return false;
            if (count == 1)
                counts.remove(id);
            else
                counts.put(id, count - 1);
        }
        return counts.isEmpty();
    }

    /**
     * Every incoming id is already stored. Extra stored ids are ignored.
     * A null id is not comparable and yields false.
     */
    public static boolean containsAllIds(Collection<UUID> stored, Collection<UUID> incoming) {
        if (stored == null || incoming == null)
            return false;
        Set<UUID> storedIds = new HashSet<>();
        for (UUID id : stored) {
            if (id == null)
                return false;
            storedIds.add(id);
        }
        for (UUID id : incoming) {
            if (id == null || !storedIds.contains(id))
                return false;
        }
        return true;
    }
}
