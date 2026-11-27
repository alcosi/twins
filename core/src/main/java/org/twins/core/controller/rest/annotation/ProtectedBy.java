package org.twins.core.controller.rest.annotation;

import org.twins.core.service.permission.Permissions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to check if the current user has any of the given permissions.
 * On controller methods/types the check is performed by ProtectedByAspect.
 * On enum constants of MapperMode implementations the check is performed declaratively at request
 * parsing time by MapperContextHandlerMethodArgumentResolver (see MapperModesResolveService).
 * In both cases the check is performed using the PermissionService.currentUserHasPermission method.
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtectedBy {
    /**
     * The permissions to check. The user must have at least one of these permissions by default true.
     */
    Permissions[] value();
    boolean anyOf() default true;
}