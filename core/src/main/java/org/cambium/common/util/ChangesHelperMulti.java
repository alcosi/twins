package org.cambium.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChangesHelperMulti<T> {
    Map<T, ChangesHelper> changesHelpers = new HashMap<>();


    public void add(T entry, ChangesHelper changesHelper) {
        changesHelpers.put(entry, changesHelper);
    }

    public Set<Map.Entry<T, ChangesHelper>> entrySet() {
        return changesHelpers.entrySet();
    }
}
