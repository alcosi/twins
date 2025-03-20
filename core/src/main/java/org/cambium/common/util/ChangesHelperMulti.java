package org.cambium.common.util;

import java.util.*;

public class ChangesHelperMulti<T> {
    Map<T, ChangesHelper> changesHelpers = new HashMap<>();


    public void add(T domainEntity, ChangesHelper changesHelper) {
        changesHelpers.put(domainEntity, changesHelper);
    }

    public Set<Map.Entry<T, ChangesHelper>> entrySet() {
        return changesHelpers.entrySet();
    }
}
