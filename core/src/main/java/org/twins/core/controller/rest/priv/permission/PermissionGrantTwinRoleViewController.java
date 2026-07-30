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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.permission.PermissionGrantTwinRoleEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionGrantTwinRoleViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionGrantTwinRoleRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantTwinRoleService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Search permission grant twin role", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_GRANT_TWIN_ROLE_MANAGE, Permissions.PERMISSION_GRANT_TWIN_ROLE_VIEW})
public class PermissionGrantTwinRoleViewController extends ApiController {
    private final PermissionGrantTwinRoleService permissionGrantTwinRoleService;
    private final PermissionGrantTwinRoleRestDTOMapper permissionGrantTwinRoleRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantTwinRoleViewV1", summary = "Permission grant twin role view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant twin role", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantTwinRoleViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/permission_grant/twin_role/{grantId}/v1")
    public ResponseEntity<?> permissionGrantTwinRoleViewV1(
            @MapperContextBinding(roots = PermissionGrantTwinRoleRestDTOMapper.class, response = PermissionGrantTwinRoleViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.PERMISSION_GRANT_TWIN_ROLE_ID) @PathVariable("grantId") UUID grantId) {
        PermissionGrantTwinRoleViewRsDTOv1 rs = new PermissionGrantTwinRoleViewRsDTOv1();
        try {
            PermissionGrantTwinRoleEntity permissionGrantTwinRole = permissionGrantTwinRoleService.findEntitySafe(grantId);

            rs
                    .setPermissionGrantTwin(permissionGrantTwinRoleRestDTOMapper.convert(permissionGrantTwinRole, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
