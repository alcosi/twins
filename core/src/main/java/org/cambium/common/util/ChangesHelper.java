package org.cambium.common.util;

import java.util.ArrayList;
import java.util.List;

public class ChangesHelper {
    List<String> changes;
    boolean hasChanges = false;

    public ChangesHelper add(String field, String oldValue, String newValue) {
        if (changes == null)
            changes = new ArrayList<>();
        hasChanges = true;
        changes.add("<" + field + ">: [" + oldValue + "] -> [" + newValue + "]");
        return this;
    }

    public ChangesHelper add(String field, Object oldValue, Object newValue) {
        return add(field, String.valueOf(oldValue), String.valueOf(newValue));
    }

    public boolean isChanged(String field, Object oldValue, Object newValue) {
        if (newValue != null && !newValue.equals(oldValue)) {
            add(field, oldValue, newValue);
            return true;
        }
        return false;
    }

    public void flush() {
        hasChanges = false;
        changes.clear();
    }

    public boolean hasChanges() {
        return hasChanges;
    }

    public String collectForLog() {
        return hasChanges ? String.join(", ", changes) : "<no changes>";
    }
}
