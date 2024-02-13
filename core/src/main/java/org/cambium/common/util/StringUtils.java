package org.cambium.common.util;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.Map;

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

}
