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
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryEraserSaveRsDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantUserSaveRsDTOV1;
import org.twins.core.dto.rest.permission.PermissionGrantUserUpdateRqDTOv1;
import org.twins.core.mappers.rest.factory.FactoryEraserRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionGrantUserRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantUserUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantUserService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Update permission grant user", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_GRANT_USER_MANAGE, Permissions.PERMISSION_GRANT_USER_UPDATE})
public class PermissionGrantUserUpdateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final PermissionGrantUserUpdateDTOReverseMapper permissionGrantUserUpdateDTOReverseMapper;
    private final PermissionGrantUserRestDTOMapper permissionGrantUserRestDTOMapper;
    private final PermissionGrantUserService permissionGrantUserService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantUserUpdateV1", summary = "Update permission grant user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant user was updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantUserSaveRsDTOV1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/permission_grant/user/{permissionGrantUserId}/v1")
    public ResponseEntity<?> permissionGrantUserUpdateV1(
            @MapperContextBinding(roots = FactoryEraserRestDTOMapper.class, response = FactoryEraserSaveRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.PERMISSION_GRANT_USER_ID) @PathVariable UUID permissionGrantUserId,
            @RequestBody PermissionGrantUserUpdateRqDTOv1 request) {
        PermissionGrantUserSaveRsDTOV1 rs = new PermissionGrantUserSaveRsDTOV1();
        try {
            PermissionGrantUserEntity entity = permissionGrantUserUpdateDTOReverseMapper.convert(request.getPermissionGrantUser());
            entity = permissionGrantUserService.updatePermissionGrantUser(entity.setId(permissionGrantUserId));
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
