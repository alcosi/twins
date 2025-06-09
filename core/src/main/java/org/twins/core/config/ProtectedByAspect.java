package org.twins.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.cambium.common.exception.ServiceException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ProtectedByAspect {
    private final PermissionService permissionService;

    @Around("@annotation(org.twins.core.controller.rest.annotation.ProtectedBy) || @within(org.twins.core.controller.rest.annotation.ProtectedBy)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ProtectedBy annotation = resolveAnnotation(method);
        if (annotation != null) {
            List<Permissions> requiredButAbsentPermissions = getRequiredButAbsentPermissions(annotation.value(), annotation.anyOf());
            if (!requiredButAbsentPermissions.isEmpty()) {
                String noPermissionNames = requiredButAbsentPermissions.stream().map(Permissions::name).collect(Collectors.joining(","));
                log.warn("Access denied: User does not have any of the required permissions {} for {}", noPermissionNames, method);
                throw new ServiceException(ErrorCodeTwins.NO_REQUIRED_PERMISSION, "User does not have required permissions [" + noPermissionNames + "]");
            }
        }
        return joinPoint.proceed();
    }

    @NotNull
    private List<Permissions> getRequiredButAbsentPermissions(Permissions[] requiredPermissions, boolean anyOf) throws ServiceException {
        if (requiredPermissions == null || requiredPermissions.length == 0) {
            return new ArrayList<>();
        }
        List<Permissions> requiredButAbsentPermissions = new ArrayList<>();
        try {
            for (Permissions permission : requiredPermissions) {
                if (!permissionService.currentUserHasPermission(permission)) {
                    requiredButAbsentPermissions.add(permission);
                } else if (anyOf) {
                    return new ArrayList<>();
                }
            }
        } catch (Throwable e) {
            log.error("Error checking permissions", e);
            throw e;
        }
        return requiredButAbsentPermissions;
    }

    protected ProtectedBy resolveAnnotation(Method method) {
        // Check for method-level annotation
        ProtectedBy methodAnnotation = method.getAnnotation(ProtectedBy.class);
        // If not found, check for class-level annotation
        ProtectedBy classAnnotation = method.getDeclaringClass().getAnnotation(ProtectedBy.class);
        ProtectedBy annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
        return annotation;
    }
}
