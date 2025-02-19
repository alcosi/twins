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
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupCreateRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupCreateRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionGrantUserGroupCreateRestReverseDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantUserGroupRestDTOMapperV2;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantUserGroupService;

@Tag(description = "", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PermissionGrantUserGroupCreateController extends ApiController {

    private final PermissionGrantUserGroupCreateRestReverseDTOMapper permissionGrantUserGroupCreateRestReverseDTOMapper;
    private final PermissionGrantUserGroupRestDTOMapperV2 permissionGrantUserGroupRestDTOMapperV2;
    private final PermissionGrantUserGroupService permissionGrantUserGroupService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantUserGroupCreateV1", summary = "Create permission grant user group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantUserGroupCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/user_group/v1")
    public ResponseEntity<?> permissionGrantUserGroupCreateV1(
            @MapperContextBinding(roots = PermissionGrantUserGroupRestDTOMapperV2.class, response = PermissionGrantUserGroupCreateRsDTOv1.class) MapperContext mapperContext,
            @RequestBody PermissionGrantUserGroupCreateRqDTOv1 request) {
        PermissionGrantUserGroupCreateRsDTOv1 rs = new PermissionGrantUserGroupCreateRsDTOv1();
        try {
            PermissionGrantUserGroupEntity entity = permissionGrantUserGroupCreateRestReverseDTOMapper.convert(request, mapperContext);
            entity = permissionGrantUserGroupService.createPermissionGrantUserGroup(entity);
            rs
                    .setPermissionGrantUserGroup(permissionGrantUserGroupRestDTOMapperV2.convert(entity))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
