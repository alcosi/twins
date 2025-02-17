package org.cambium.featurer.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface FeaturerParam {
    public static String DEFAULT_VALUE_NOT_SET = "<default-value-not-set>";
    String name();

    int order() default 1;

    String description() default "";

    boolean optional() default false;

    String defaultValue() default DEFAULT_VALUE_NOT_SET;

    String[] exampleValues() default {};
}
