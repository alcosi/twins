# @ProtectedBy Annotation

The `@ProtectedBy` annotation is used to check if the current user has any of the given permissions. The check is
performed using the `PermissionService.currentUserHasPermission` method.

## Usage

The `@ProtectedBy` annotation can be applied to methods or classes. When applied to a class, all methods in the class
will be protected by the specified permissions.

```java
// Protect a method
@ProtectedBy(Permissions.TWIN_CLASS_MANAGE)
public ResponseEntity<TwinClassRestDTOv1> createTwinClass(@RequestBody TwinClassRestDTOv1 twinClassDTO) {
    // Method implementation
}

// Protect a class
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE, Permissions.TWIN_CLASS_VIEW})
public class TwinClassController {
    // Class implementation
}
```

## Parameters

The `@ProtectedBy` annotation takes an array of `Permissions` enum values as its parameter. The user must have at least
one of the specified permissions to access the protected method or class.

```java
// Example of using multiple permissions
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE, Permissions.TWIN_CLASS_VIEW})
public void someMethod() {
    // Method implementation
}
```

## Error Handling

If the user does not have any of the required permissions, a `ServiceException` with the error code
`DOMAIN_PERMISSION_DENIED` will be thrown.

## Implementation Details

The `@ProtectedBy` annotation is implemented using AspectJ. The `ProtectedByAspect` class intercepts method calls
annotated with `@ProtectedBy` and checks if the current user has any of the specified permissions.

```java
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ProtectedByAspect {

    private final PermissionService permissionService;

    @Around("@annotation(org.twins.core.controller.rest.annotation.ProtectedBy) || @within(org.twins.core.controller.rest.annotation.ProtectedBy)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // Implementation details
    }
}
```

The aspect checks both method-level and class-level annotations, and throws a `ServiceException` if the user does not
have any of the required permissions.
