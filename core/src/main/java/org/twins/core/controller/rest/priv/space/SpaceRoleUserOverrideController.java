package org.twins.core.controller.rest.priv.space;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.space.SpaceRoleUserOverrideRqDTOv1;
import org.twins.core.dto.rest.user.UserListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.space.SpaceUserRoleService;

import java.util.UUID;

@Tag(name = ApiTag.SPACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.SPACE_ROLE_MANAGE)
public class SpaceRoleUserOverrideController extends ApiController {
    private final UserRestDTOMapper userRestDTOMapper;
    private final SpaceUserRoleService spaceUserRoleService;

    @ParametersApiUserHeaders
    @Operation(operationId = "spaceRoleUserOverrideV1", summary = "Add/Remove user list by role and twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/space/{spaceId}/role/{roleId}/users/override/v1")
    public ResponseEntity<?> spaceRoleUserOverrideV1(
            @MapperContextBinding(roots = UserRestDTOMapper.class, response = UserListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID spaceId,
            @Parameter(example = DTOExamples.ROLE_ID) @PathVariable UUID roleId,
            @RequestBody SpaceRoleUserOverrideRqDTOv1 request) {
        UserListRsDTOv1 rs = new UserListRsDTOv1();
        try {
            spaceUserRoleService.overrideSpaceRoleUsers(spaceId, roleId, request.spaceRoleUserList);
            rs.userList = userRestDTOMapper.convertCollection(
                    spaceUserRoleService.findUserBySpaceIdAndRoleId(spaceId, roleId), mapperContext);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
