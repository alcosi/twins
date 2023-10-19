/*
 * Copyright (c)
 * created:2021 - 12 - 1
 * by Yan Tayanouski
 * ESAS Ltd. La propriété, c'est le vol!
 */

package org.twins.core.controller.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Loggable {
    boolean value() default true;
}
