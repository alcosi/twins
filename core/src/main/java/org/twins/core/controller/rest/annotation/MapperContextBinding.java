package org.twins.core.controller.rest.annotation;

import org.twins.core.mappers.rest.RestDTOMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapperContextBinding {
    Class<? extends RestDTOMapper> root();
    boolean lazySupport() default true;
}
