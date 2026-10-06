package org.cambium.featurer.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface FeaturerParamType {
    String id();

    String description();

    String regexp();

    String example();

    /**
     * Entity class referenced by values of this param type, when the param value is an entity id
     * (or a set/list of entity ids). Used by the mapping layer to resolve param values into
     * relatedObjects. Absent (Void.class) for non-entity-referencing param types.
     */
    Class<?> targetEntity() default Void.class;
}
