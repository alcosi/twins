package org.twins.core.featurer.fieldtyper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FieldValueChangeHelper {
    public static boolean isSingleValueUpdate(List<?> newValueList, Map<UUID, ?> oldValueMap) {
        return newValueList != null && newValueList.size() == 1 && oldValueMap != null && oldValueMap.size() == 1;
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
}
