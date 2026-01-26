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
import org.twins.core.dao.permission.PermissionGrantTwinRoleEntity;
import org.twins.core.dto.rest.permission.PermissionGrantTwinRoleCreateRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantTwinRoleRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionGrantTwinRoleCreateDTOReverseMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantTwinRoleRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantTwinRoleService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "Create permission grant twin role", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_GRANT_TWIN_ROLE_MANAGE, Permissions.PERMISSION_GRANT_TWIN_ROLE_CREATE})
public class PermissionGrantTwinRoleCreateController extends ApiController {
    private final PermissionGrantTwinRoleService permissionGrantTwinRoleService;
    private final PermissionGrantTwinRoleRestDTOMapper permissionGrantTwinRoleRestDTOMapper;
    private final PermissionGrantTwinRoleCreateDTOReverseMapper permissionGrantTwinRoleCreateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantTwinRoleCreateV1", summary = "permission grant twin role create add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "permission grant twin role create add", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantTwinRoleRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/twin_role/v1")
    public ResponseEntity<?> permissionGrantTwinRoleCreateV1(
            @MapperContextBinding(roots = PermissionGrantTwinRoleRestDTOMapper.class, response = PermissionGrantTwinRoleRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody PermissionGrantTwinRoleCreateRqDTOv1 request) {
        PermissionGrantTwinRoleRsDTOv1 rs = new PermissionGrantTwinRoleRsDTOv1();
        try {
            PermissionGrantTwinRoleEntity permissionGrantTwinRole = permissionGrantTwinRoleService.createPermissionGrantTwinRole
                    (permissionGrantTwinRoleCreateDTOReverseMapper.convert(request.getPermissionGrantTwinRole()));
            rs
                    .setPermissionGrantTwinRole(permissionGrantTwinRoleRestDTOMapper.convert(permissionGrantTwinRole, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
