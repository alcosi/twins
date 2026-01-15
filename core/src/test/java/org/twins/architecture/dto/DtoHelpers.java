package org.twins.architecture.dto;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public class DtoHelpers {
    public static ArchCondition<JavaClass> mustHaveField(String fieldName) {
        return new ArchCondition<>("have field '" + fieldName + "'") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                boolean hasField = clazz.getFields().stream()
                        .anyMatch(f -> f.getName().equals(fieldName));
                if (!hasField) {
                    events.add(SimpleConditionEvent.violated(
                            clazz,
                            clazz.getSimpleName() + " must have field '" + fieldName + "'"
                    ));
                }
            }
        };
    }
}
