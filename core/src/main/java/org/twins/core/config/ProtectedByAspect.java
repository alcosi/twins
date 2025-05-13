package org.twins.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;

import java.lang.reflect.Method;

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

        // Check for method-level annotation
        ProtectedBy methodAnnotation = method.getAnnotation(ProtectedBy.class);
        // If not found, check for class-level annotation
        ProtectedBy classAnnotation = method.getDeclaringClass().getAnnotation(ProtectedBy.class);

        ProtectedBy annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;

        if (annotation != null) {
            Permissions[] requiredPermissions = annotation.value();

            // Check if the user has any of the required permissions
            boolean hasPermission = false;
            for (Permissions permission : requiredPermissions) {
                try {
                    if (permissionService.currentUserHasPermission(permission)) {
                        hasPermission = true;
                        break;
                    }
                } catch (ServiceException e) {
                    log.error("Error checking permission: {}", permission, e);
                    throw e;
                }
            }

            if (!hasPermission) {
                log.warn("Access denied: User does not have any of the required permissions for {}", method);
                throw new ServiceException(ErrorCodeTwins.DOMAIN_PERMISSION_DENIED, "User does not have any of the required permissions");
            }
        }

        return joinPoint.proceed();
    }
}
