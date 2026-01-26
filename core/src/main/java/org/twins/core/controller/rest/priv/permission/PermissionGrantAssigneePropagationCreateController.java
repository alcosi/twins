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
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationCreateRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionGrantAssigneePropagationCreateDTOReverseMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantAssigneePropagationRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantAssigneePropagationService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "Create permission grant assignee propagation", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_GRANT_ASSIGNEE_PROPAGATION_MANAGE, Permissions.PERMISSION_GRANT_ASSIGNEE_PROPAGATION_CREATE})
public class PermissionGrantAssigneePropagationCreateController extends ApiController {
    private final PermissionGrantAssigneePropagationService permissionGrantAssigneePropagationService;
    private final PermissionGrantAssigneePropagationRestDTOMapper permissionGrantAssigneePropagationRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PermissionGrantAssigneePropagationCreateDTOReverseMapper permissionGrantAssigneePropagationCreateDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantAssigneePropagationCreateV1", summary = "permission grantAssignee propagation create add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "permission grant assignee propagation add", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantAssigneePropagationRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/assignee_propagation/v1")
    public ResponseEntity<?> permissionGrantAssigneePropagationCreateV1(
            @MapperContextBinding(roots = PermissionGrantAssigneePropagationRestDTOMapper.class, response = PermissionGrantAssigneePropagationRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody PermissionGrantAssigneePropagationCreateRqDTOv1 request) {
        PermissionGrantAssigneePropagationRsDTOv1 rs = new PermissionGrantAssigneePropagationRsDTOv1();
        try {
            PermissionGrantAssigneePropagationEntity permissionGrantAssigneePropagation = permissionGrantAssigneePropagationService.createPermissionGrantAssigneePropagationEntity
                    (permissionGrantAssigneePropagationCreateDTOReverseMapper.convert(request.getPermissionGrantAssigneePropagation()));
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
