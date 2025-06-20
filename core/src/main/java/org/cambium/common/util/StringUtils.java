package org.cambium.common.util;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.*;

public class StringUtils extends org.apache.commons.lang3.StringUtils {
    public static String replaceVariables(String str, Map<String, String> map) {
        return replaceVariables(str, StrLookup.mapLookup(map));
    }

    public static String replaceVariables(String str, StrLookup strLookup) {
        StrSubstitutor s = new StrSubstitutor();
        s.setVariablePrefix("${");
        s.setVariableSuffix("}");
        s.setVariableResolver(strLookup);
        return s.replace(str);
    }

    public static String fmt(double d)
    {
        if(d == (long) d)
            return String.format("%d",(long)d);
        else
            return String.format("%s",d);
    }

    public static String tabs(int i) {
        return switch (i) {
            case 0 -> "";
            case 1 -> "\t";
            case 2 -> "\t\t";
            case 3 -> "\t\t\t";
            case 4 -> "\t\t\t\t";
            case 5 -> "\t\t\t\t\t";
            default -> "\t\t\t\t\t\t";
        };
    }

    public static Set<String> splitToSet(String str, String delimiter) {
        if (isEmpty(str))
            return Collections.EMPTY_SET;
        return new HashSet<>(Arrays.asList(str.split(delimiter)));
    }
}
