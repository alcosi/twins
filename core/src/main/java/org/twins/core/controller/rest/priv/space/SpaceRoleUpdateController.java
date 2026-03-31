package org.twins.core.controller.rest.priv.space;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.domain.space.SpaceRoleUpdate;
import org.twins.core.dto.rest.space.SpaceRoleListRsDTOv1;
import org.twins.core.dto.rest.space.SpaceRoleUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.space.SpaceRoleDTOMapper;
import org.twins.core.mappers.rest.space.SpaceRoleUpdateDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.space.SpaceRoleService;

import java.util.List;

@Tag(description = "", name = ApiTag.SPACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.SPACE_ROLE_UPDATE)
public class SpaceRoleUpdateController extends ApiController {
    private final SpaceRoleUpdateDTOReverseMapper spaceRoleUpdateDTOReverseMapper;
    private final SpaceRoleDTOMapper spaceRoleDTOMapper;
    private final SpaceRoleService spaceRoleService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "spaceRoleUpdateV1", summary = "Update batch space role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The space role batch was updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = SpaceRoleListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/space_role/v1")
    public ResponseEntity<?> spaceRoleUpdateV1(
            @MapperContextBinding(roots = SpaceRoleDTOMapper.class, response = SpaceRoleListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody SpaceRoleUpdateRqDTOv1 request) {
        SpaceRoleListRsDTOv1 rs = new SpaceRoleListRsDTOv1();
        try {
            List<SpaceRoleUpdate> updateList = spaceRoleUpdateDTOReverseMapper.convertCollection(request.getSpaceRoles());
            List<SpaceRoleEntity> spaceRoleList = spaceRoleService.updateSpaceRole(updateList);
            rs
                    .setSpaceRoles(spaceRoleDTOMapper.convertCollection(spaceRoleList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
