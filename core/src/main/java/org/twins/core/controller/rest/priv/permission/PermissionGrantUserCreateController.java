package org.twins.core.controller.rest.priv.permission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserCreateRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantUserSaveRsDTOV1;
import org.twins.core.mappers.rest.factory.FactoryEraserRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionGrantUserCreateDTOReverseMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantUserRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantUserService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "Create permission grant user", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_GRANT_USER_MANAGE, Permissions.PERMISSION_GRANT_USER_CREATE})
public class PermissionGrantUserCreateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final PermissionGrantUserRestDTOMapper permissionGrantUserRestDTOMapper;
    private final PermissionGrantUserCreateDTOReverseMapper permissionGrantUserCreateDTOReverseMapper;
    private final PermissionGrantUserService permissionGrantUserService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantUserCreateV1", summary = "Create permission grant user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant user created successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantUserSaveRsDTOV1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/user/v1")
    public ResponseEntity<?> permissionGrantUserCreateV1(
            @MapperContextBinding(roots = FactoryEraserRestDTOMapper.class, response = PermissionGrantUserSaveRsDTOV1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody PermissionGrantUserCreateRqDTOv1 request) {
        PermissionGrantUserSaveRsDTOV1 rs = new PermissionGrantUserSaveRsDTOV1();
        try {
            PermissionGrantUserEntity entity = permissionGrantUserCreateDTOReverseMapper.convert(request.getPermissionGrantUser(), mapperContext);
            entity = permissionGrantUserService.createPermissionGrantUser(entity);
            rs
                    .setPermissionGrantUser(permissionGrantUserRestDTOMapper.convert(entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
