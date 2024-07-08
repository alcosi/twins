package org.twins.core.controller.rest.annotation;

import org.twins.core.dto.rest.Response;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.RestDTOMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapperContextBinding {
    Class<? extends RestDTOMapper<?, ?>>[] roots();
    Class<? extends Response> response();
    Class<? extends MapperMode>[] block() default {};
}
