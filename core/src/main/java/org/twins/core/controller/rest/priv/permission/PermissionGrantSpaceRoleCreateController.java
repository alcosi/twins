package org.twins.core.controller.rest.priv.permission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletRequest;
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
import org.twins.core.dao.permission.PermissionGrantSpaceRoleEntity;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleCreateRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionGrantSpaceRoleCreateDTOReverseMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantSpaceRoleRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantSpaceRoleService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "Create permission grant space role", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_GRANT_SPACE_ROLE_MANAGE, Permissions.PERMISSION_GRANT_SPACE_ROLE_CREATE})
public class PermissionGrantSpaceRoleCreateController extends ApiController {
    private final PermissionGrantSpaceRoleService permissionGrantSpaceRoleService;
    private final PermissionGrantSpaceRoleRestDTOMapper permissionGrantSpaceRoleRestDTOMapper;
    private final PermissionGrantSpaceRoleCreateDTOReverseMapper permissionGrantSpaceRoleCreateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantSpaceRoleCreateV1", summary = "permission grant space role add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "permission grant space role add", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantSpaceRoleRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/space_role/v1")
    public ResponseEntity<?> permissionGrantSpaceRoleCreateV1(
            @MapperContextBinding(roots = PermissionGrantSpaceRoleRestDTOMapper.class, response = PermissionGrantSpaceRoleRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody PermissionGrantSpaceRoleCreateRqDTOv1 request, ServletRequest servletRequest) {
        PermissionGrantSpaceRoleRsDTOv1 rs = new PermissionGrantSpaceRoleRsDTOv1();
        try {
            PermissionGrantSpaceRoleEntity permissionGrantSpaceRole = permissionGrantSpaceRoleService
                    .createPermissionGrantSpaceRole(permissionGrantSpaceRoleCreateDTOReverseMapper.convert(request.getPermissionGrantSpaceRole()));
            rs
                    .setPermissionGrantSpaceRole(permissionGrantSpaceRoleRestDTOMapper.convert(permissionGrantSpaceRole,mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));

        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
