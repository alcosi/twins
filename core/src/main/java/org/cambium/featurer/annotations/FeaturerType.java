package org.cambium.featurer.annotations;

import java.lang.annotation.*;

@Inherited
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface FeaturerType {
    int id();

    String name();

    String description();
}
