package org.twins.core.controller.rest.annotation;

import org.twins.core.mappers.rest.MapperMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapperModeBinding {
    Class<? extends MapperMode>[] modes();
}
