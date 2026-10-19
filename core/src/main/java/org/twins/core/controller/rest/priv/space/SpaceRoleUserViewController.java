package org.twins.core.controller.rest.priv.space;

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
import org.springframework.web.server.ResponseStatusException;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.domain.space.UserRefSpaceRole;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.space.UserWithinSpaceRolesViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.space.UserRefSpaceRoleDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.space.SpaceRoleUserService;

import java.util.UUID;

@Tag(description = "Get space role user", name = ApiTag.SPACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.SPACE_ROLE_MANAGE, Permissions.SPACE_ROLE_VIEW})
public class SpaceRoleUserViewController extends ApiController {
    private final SpaceRoleUserService spaceRoleUserService;
    private final UserRefSpaceRoleDTOMapper userRefSpaceRoleDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "spaceRoleUserViewV1", summary = "Get user within his roles of specific space")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserWithinSpaceRolesViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/space/{spaceId}/users/{userId}/v1")
    public ResponseEntity<?> spaceRoleUserViewV1(
            @MapperContextBinding(roots = UserRefSpaceRoleDTOMapper.class, response = UserWithinSpaceRolesViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.SPACE_ID) @PathVariable UUID spaceId,
            @Parameter(example = DTOExamples.PERMISSION_ID) @PathVariable("userId") UUID userId) {
        UserWithinSpaceRolesViewRsDTOv1 rs = new UserWithinSpaceRolesViewRsDTOv1();
        try {
            UserRefSpaceRole userRefRolesMap = spaceRoleUserService.getUsersRefRolesMapById(spaceId, userId);
            if (userRefRolesMap.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such user: " + userId+ " in current domain." );
            }
            rs.setUserRefSpaceRoles(userRefSpaceRoleDTOMapper.convert(userRefRolesMap, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }


}
