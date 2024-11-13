package org.cambium.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChangesHelper {
    List<String> changes;
    boolean hasChanges = false;

    public ChangesHelper() {
        changes = new ArrayList<>();
    }

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

    public ChangesHelper addWithNullifySupport(String field, Object oldValue, Object newValue) {
        if (newValue instanceof UUID && UuidUtils.NULLIFY_MARKER.equals(newValue)) {
            add(field, oldValue, null);
        } else
            add(field, oldValue, newValue);
        return this;
    }

    public boolean isChanged(String field, Object oldValue, Object newValue) {
        if (newValue != null && !newValue.equals(oldValue)) {
            addWithNullifySupport(field, oldValue, newValue);
            return true;
        }
        return false;
    }

    public boolean isChanged(String field, Object oldValue, Object newValue, String oldMaskedValue, String newMaskedValue) {
        if (newValue != null && !newValue.equals(oldValue)) {
            add(field, oldMaskedValue, newMaskedValue);
            return true;
        }
        return false;
    }

    public void flush() {
        hasChanges = false;
        changes.clear();
    }

    public boolean hasChange(String field) {
        for (String change : changes)
            if(change.contains("<" + field + ">"))
                return true;
        return false;
    }

    public boolean hasChanges() {
        return hasChanges;
    }

    public String collectForLog() {
        return hasChanges ? String.join(", ", changes) : "<no changes>";
    }
}
