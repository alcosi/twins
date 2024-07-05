package org.twins.core.controller.rest.annotation;

import org.twins.core.mappers.rest.mappercontext.MapperMode;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MapperModeBinding {
    Class<? extends MapperMode>[] modes();
}
