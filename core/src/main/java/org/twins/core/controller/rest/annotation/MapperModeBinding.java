package org.twins.core.controller.rest.annotation;

import org.twins.core.mappers.rest.MapperMode;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MapperModeBinding {
    Class<? extends MapperMode>[] modes();
}
