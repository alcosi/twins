package org.cambium.common.util;

public class TernaryUtils {
    public static Ternary narrow(Ternary mainSearch, Ternary narrowSearch) {
        if (mainSearch == null || mainSearch == Ternary.ANY) {
            return narrowSearch;
        } else {
            return mainSearch;
        }
    }
}
