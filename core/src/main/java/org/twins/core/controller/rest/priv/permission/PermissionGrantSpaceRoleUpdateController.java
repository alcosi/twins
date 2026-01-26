package org.twins.core.controller.rest.priv.permission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.permission.PermissionGrantSpaceRoleEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleRsDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionGrantSpaceRoleRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantSpaceRoleUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantSpaceRoleService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Update permission grant space role", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_GRANT_SPACE_ROLE_MANAGE, Permissions.PERMISSION_GRANT_SPACE_ROLE_UPDATE})
public class PermissionGrantSpaceRoleUpdateController extends ApiController {
    private final PermissionGrantSpaceRoleService permissionGrantSpaceRoleService;
    private final PermissionGrantSpaceRoleRestDTOMapper permissionGrantSpaceRoleRestDTOMapper;
    private final PermissionGrantSpaceRoleUpdateDTOReverseMapper permissionGrantSpaceRoleUpdateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantSpaceRoleUpdateV1", summary = "Permission grant space role update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant space role update", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantSpaceRoleRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/permission_grant/space_role/{permissionGrantSpaceRoleId}/v1")
    public ResponseEntity<?> permissionGrantSpaceRoleV1(
            @MapperContextBinding(roots = PermissionGrantSpaceRoleRestDTOMapper.class, response = PermissionGrantSpaceRoleRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.PERMISSION_GRANT_SPACE_ROLE_ID) @PathVariable UUID permissionGrantSpaceRoleId,
            @RequestBody PermissionGrantSpaceRoleUpdateRqDTOv1 request) {

        PermissionGrantSpaceRoleRsDTOv1 rs = new PermissionGrantSpaceRoleRsDTOv1();
        try {
            PermissionGrantSpaceRoleEntity permissionGrantSpaceRole = permissionGrantSpaceRoleUpdateDTOReverseMapper.convert(request.getPermissionGrantSpaceRole())
                    .setId(permissionGrantSpaceRoleId);
            permissionGrantSpaceRole = permissionGrantSpaceRoleService.updatePermissionGrantSpaceRole(permissionGrantSpaceRole);

            rs
                    .setPermissionGrantSpaceRole(permissionGrantSpaceRoleRestDTOMapper.convert(permissionGrantSpaceRole, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
