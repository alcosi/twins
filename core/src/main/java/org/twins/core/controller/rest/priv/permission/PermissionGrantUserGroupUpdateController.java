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
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupSaveRsDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionGrantUserGroupRestDTOMapperV2;
import org.twins.core.mappers.rest.permission.PermissionGrantUserGroupUpdateRestReverseDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantUserGroupService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PermissionGrantUserGroupUpdateController extends ApiController {
    private final PermissionGrantUserGroupService permissionGrantUserGroupService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final PermissionGrantUserGroupUpdateRestReverseDTOMapper permissionGrantUserGroupUpdateRestReverseDTOMapper;
    private final PermissionGrantUserGroupRestDTOMapperV2 permissionGrantUserGroupRestDTOMapperV2;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantUserGroupUpdateV1", summary = "Update permission grant user group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantUserGroupSaveRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/permission_grant/user_group/{permissionGrantUserGroupId}/v1")
    public ResponseEntity<?> permissionGrantUserGroupUpdateV1(
            @MapperContextBinding(roots = PermissionGrantUserGroupRestDTOMapperV2.class, response = PermissionGrantUserGroupSaveRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.PERMISSION_GRANT_USER_GROUP_ID) @PathVariable UUID permissionGrantUserGroupId,
            @RequestBody PermissionGrantUserGroupUpdateRqDTOv1 request) {
        PermissionGrantUserGroupSaveRsDTOv1 rs = new PermissionGrantUserGroupSaveRsDTOv1();
        try {
            PermissionGrantUserGroupEntity entity = permissionGrantUserGroupUpdateRestReverseDTOMapper.convert(request.getPermissionGrantUserGroupUpdate());
            entity = permissionGrantUserGroupService.updatePermissionGrantUserGroup(entity.setId(permissionGrantUserGroupId));
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
