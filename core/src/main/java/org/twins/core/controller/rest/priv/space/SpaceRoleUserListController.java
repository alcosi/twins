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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.domain.space.UsersRefSpaceRolePageable;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.space.UserRefSpaceRoleSearchDTOv1;
import org.twins.core.dto.rest.space.UserWithinSpaceRolesListRsDTOv1;
import org.twins.core.dto.rest.user.UserListRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.space.SpaceRoleUserSearchRqDTOReverseMapper;
import org.twins.core.mappers.rest.space.UserRefSpaceRoleDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.space.SpaceUserRoleService;

import java.util.UUID;

import static org.cambium.common.util.PaginationUtils.DEFAULT_VALUE_LIMIT;
import static org.cambium.common.util.PaginationUtils.DEFAULT_VALUE_OFFSET;

@Tag(name = ApiTag.SPACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class SpaceRoleUserListController extends ApiController {
    private final UserRestDTOMapper userRestDTOMapper;
    private final UserRefSpaceRoleDTOMapper userRefSpaceRoleDTOMapper;
    private final PaginationMapper paginationMapper;
    private final SpaceUserRoleService spaceUserRoleService;
    private final SpaceRoleUserSearchRqDTOReverseMapper userSearchRqDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "spaceRoleByUserListV1", summary = "Returns user list by selected space and role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/space/{spaceId}/role/{roleId}/users/v1")
    public ResponseEntity<?> spaceRoleForUserListV1(
            @MapperContextBinding(roots = UserRestDTOMapper.class, response = UserListRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.SPACE_ID) @PathVariable UUID spaceId,
            @Parameter(example = DTOExamples.ROLE_ID) @PathVariable UUID roleId) {
        UserListRsDTOv1 rs = new UserListRsDTOv1();
        try {
            rs.userList = userRestDTOMapper.convertCollection(spaceUserRoleService.findUserByRole(spaceId, roleId), mapperContext);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "spaceRoleWithinAllUsersMapV1", summary = "Return all users within roles of specific space")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserWithinSpaceRolesListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/space/{spaceId}/users/list/v1")
    public ResponseEntity<?> spaceUserListV1(
            @MapperContextBinding(roots = UserRefSpaceRoleDTOMapper.class, response = UserWithinSpaceRolesListRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = "5d956a15-6858-40ba-b0aa-b123c54e250d") @PathVariable UUID spaceId,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit) {
        UserWithinSpaceRolesListRsDTOv1 rs = new UserWithinSpaceRolesListRsDTOv1();
        try {
            UsersRefSpaceRolePageable usersRefRoles = spaceUserRoleService.getAllUsersRefRolesBySpaceIdMap(spaceId, offset, limit);
            rs
                    .setUsersRefSpaceRolesList(userRefSpaceRoleDTOMapper.convertCollection(usersRefRoles.getUsersRefRoles(), mapperContext))
                    .setPagination(paginationMapper.convert(usersRefRoles))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "spaceRoleWithinUsersMapV1", summary = "Search users within their roles of specific space")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserWithinSpaceRolesListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/space/{spaceId}/users/search/v1")
    public ResponseEntity<?> spaceRoleUserSearchV1(
            @MapperContextBinding(roots = UserRefSpaceRoleDTOMapper.class, response = UserWithinSpaceRolesListRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = "5d956a15-6858-40ba-b0aa-b123c54e250d") @PathVariable UUID spaceId,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit,
            @RequestBody UserRefSpaceRoleSearchDTOv1 request) {
        UserWithinSpaceRolesListRsDTOv1 rs = new UserWithinSpaceRolesListRsDTOv1();
        try {
            UsersRefSpaceRolePageable usersRefRoles = spaceUserRoleService.getUsersRefRolesMap(userSearchRqDTOReverseMapper.convert(request), spaceId, offset, limit);
            rs
                    .setUsersRefSpaceRolesList(userRefSpaceRoleDTOMapper.convertCollection(usersRefRoles.getUsersRefRoles(), mapperContext))
                    .setPagination(paginationMapper.convert(usersRefRoles))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
