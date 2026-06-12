package org.cambium.common;

import java.util.ArrayList;

public class StringList extends ArrayList<String> {
    public boolean addNotBlank(String s) {
        if (s == null || s.isBlank()) return false;
        return super.add(s);
    }
}
