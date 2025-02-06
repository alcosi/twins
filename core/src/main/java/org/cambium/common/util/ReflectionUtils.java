package org.cambium.common.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReflectionUtils {
    public static List<Field> getAllDeclaredFieldsIncludingParents(Class<?> featurerClass) {
        List<Field> allFields = Arrays.stream(featurerClass.getFields()).distinct().collect(Collectors.toList());
        Class<?> superClass = featurerClass.getSuperclass();
        while (superClass != null) {
            allFields.addAll(Arrays.asList(superClass.getFields()));
            superClass = superClass.getSuperclass();
        }
        return allFields;
    }
}
