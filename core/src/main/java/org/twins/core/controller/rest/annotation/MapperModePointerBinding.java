package org.twins.core.controller.rest.annotation;

import org.twins.core.mappers.rest.mappercontext.MapperMode;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MapperModePointerBinding {
    Class<? extends MapperMode>[] modes();
}
