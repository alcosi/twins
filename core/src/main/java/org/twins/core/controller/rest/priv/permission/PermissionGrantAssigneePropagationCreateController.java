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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationCreateRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionGrantAssigneePropagationCreateDTOReverseMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantAssigneePropagationRestDTOMapperV2;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantAssigneePropagationService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PermissionGrantAssigneePropagationCreateController extends ApiController {
    private final PermissionGrantAssigneePropagationService service;
    private final PermissionGrantAssigneePropagationRestDTOMapperV2 permissionGrantAssigneePropagationRestDTOMapperV2;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PermissionGrantAssigneePropagationCreateDTOReverseMapper permissionGrantAssigneePropagationCreateDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantAssigneePropagationCreateV1", summary = "permission grantAssignee propagation create add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "permission grantAssignee propagation create add", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantAssigneePropagationRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/assignee_propagation/v1")
    public ResponseEntity<?> permissionGrantAssigneePropagationCreateV1(
            @MapperContextBinding(roots = PermissionGrantAssigneePropagationRestDTOMapperV2.class, response = PermissionGrantAssigneePropagationRsDTOv1.class) MapperContext mapperContext,
            @RequestBody PermissionGrantAssigneePropagationCreateRqDTOv1 request) {
        PermissionGrantAssigneePropagationRsDTOv1 rs = new PermissionGrantAssigneePropagationRsDTOv1();
        try {
            PermissionGrantAssigneePropagationEntity entity = permissionGrantAssigneePropagationCreateDTOReverseMapper.convert(request);
            entity = service.createPermissionGrantAssigneePropagationEntity(entity);
            rs
                    .setPermissionGrantAssigneePropagation(permissionGrantAssigneePropagationRestDTOMapperV2.convert(entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
