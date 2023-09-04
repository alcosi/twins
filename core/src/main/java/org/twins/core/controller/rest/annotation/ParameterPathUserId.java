package org.twins.core.controller.rest.annotation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.twins.core.dto.rest.DTOExamples;

import java.lang.annotation.Inherited;

@Inherited
public @interface ParameterPathUserId {
    Parameter value() default @Parameter(name = "userId", in = ParameterIn.PATH, required = true, example = DTOExamples.USER_ID);
}
