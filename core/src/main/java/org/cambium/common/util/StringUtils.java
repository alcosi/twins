package org.cambium.common.util;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.*;
import java.util.stream.Collectors;

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

    public static String snakeToCamel(String snakeCase) {
        // Return original string if it's null, empty, or has no underscore.
        if (snakeCase == null || !snakeCase.contains("_")) {
            return snakeCase;
        }

        StringBuilder camelCase = new StringBuilder();
        // Split the string by one or more underscores
        String[] parts = snakeCase.split("_+");

        // Append the first part as is (it's already lowercase)
        camelCase.append(parts[0]);

        // Iterate from the second part onwards
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue; // Skip empty parts that can result from multiple underscores
            }
            // Capitalize the first letter and append the rest of the word
            camelCase.append(Character.toUpperCase(parts[i].charAt(0)))
                    .append(parts[i].substring(1));
        }
        return camelCase.toString();
    }

    public static String collectionToString(Collection<UUID> collection) {
        if (collection == null || collection.isEmpty()) {
            return "";
        }
        return collection.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));
    }
}
