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
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationRsDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionGrantAssigneePropagationRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantAssigneePropagationUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantAssigneePropagationService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Update permission grant assignee propagation", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_GRANT_ASSIGNEE_PROPAGATION_MANAGE, Permissions.PERMISSION_GRANT_ASSIGNEE_PROPAGATION_UPDATE})
public class PermissionGrantAssigneePropagationUpdateController extends ApiController {
    private final PermissionGrantAssigneePropagationService permissionGrantAssigneePropagationService;
    private final PermissionGrantAssigneePropagationRestDTOMapper permissionGrantAssigneePropagationRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PermissionGrantAssigneePropagationUpdateDTOReverseMapper permissionGrantAssigneePropagationUpdateDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantAssigneePropagationUpdateV1", summary = "Permission grant assignee propagation update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant assignee propagation update", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantAssigneePropagationRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/permission_grant/assingee_propagation/{permissionGrantAssigneePropagationId}/v1")
    public ResponseEntity<?> permissionGrantAssigneePropagationUpdateV1(
            @MapperContextBinding(roots = PermissionGrantAssigneePropagationRestDTOMapper.class, response = PermissionGrantAssigneePropagationRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.PERMISSION_GRANT_ASSIGNEE_PROPAGATION_ID) @PathVariable UUID permissionGrantAssigneePropagationId,
            @RequestBody PermissionGrantAssigneePropagationUpdateRqDTOv1 request) {

        PermissionGrantAssigneePropagationRsDTOv1 rs = new PermissionGrantAssigneePropagationRsDTOv1();
        try {
            PermissionGrantAssigneePropagationEntity permissionGrantAssigneePropagation = permissionGrantAssigneePropagationUpdateDTOReverseMapper.convert(request.getPermissionGrantAssigneePropagation())
                    .setId(permissionGrantAssigneePropagationId);
            permissionGrantAssigneePropagation = permissionGrantAssigneePropagationService.updatePermissionGrantAssigneePropagationEntity(permissionGrantAssigneePropagation);

            rs
                    .setPermissionGrantAssigneePropagation(permissionGrantAssigneePropagationRestDTOMapper.convert(permissionGrantAssigneePropagation, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
