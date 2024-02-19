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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.space.UserWithinSpaceRolesListRsDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.space.SpaceRoleUserSearchDTOv1;
import org.twins.core.dto.rest.user.UserListRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.space.SpaceRoleUserBaseDTOMapper;
import org.twins.core.mappers.rest.space.SpaceRoleUserDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.mappers.rest.space.SpaceRoleUserSearchRqDTOReverseMapper;

import java.util.UUID;

@Tag(name = ApiTag.SPACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class SpaceRoleUserListController extends ApiController {
    final UserRestDTOMapper userRestDTOMapper;
    final SpaceRoleUserDTOMapper spaceRoleUserDTOMapper;
    final SpaceUserRoleService spaceUserRoleService;
    final SpaceRoleUserSearchRqDTOReverseMapper userSearchRqDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "spaceRoleByUserListV1", summary = "Returns user list by selected space and role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/space/{spaceId}/role/{roleId}/users/v1")
    public ResponseEntity<?> spaceRoleForUserListV1(
            @Parameter(example = "5d956a15-6858-40ba-b0aa-b123c54e250d") @PathVariable UUID spaceId,
            @Parameter(example = DTOExamples.ROLE_ID) @PathVariable UUID roleId,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._DETAILED) UserRestDTOMapper.Mode showUserMode) {
        UserListRsDTOv1 rs = new UserListRsDTOv1();
        try {
            rs.userList = userRestDTOMapper.convertList(spaceUserRoleService.findUserByRole(spaceId, roleId), new MapperContext().setMode(showUserMode));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "spaceRoleWithinUserMapV1", summary = "Return all users within roles of specific space")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserWithinSpaceRolesListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/space/{spaceId}/users/list/v1")
    public ResponseEntity<?> spaceUserListV1(
            @Parameter(example = "5d956a15-6858-40ba-b0aa-b123c54e250d") @PathVariable UUID spaceId,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._DETAILED) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showSpaceRoleMode, defaultValue = SpaceRoleUserBaseDTOMapper.Mode._SHORT) SpaceRoleUserBaseDTOMapper.Mode spaceRoleMode) {
        UserWithinSpaceRolesListRsDTOv1 rs = new UserWithinSpaceRolesListRsDTOv1();
        try {

            rs.spaceRoleUserList = spaceRoleUserDTOMapper.convertList(
                    spaceUserRoleService.getAllUsersRefRolesBySpaceIdMap(spaceId), new MapperContext().setMode(showUserMode).setMode(spaceRoleMode)
            );
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "spaceRoleWithinUserMapV1", summary = "Search users within their roles of specific space")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserWithinSpaceRolesListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/space/{spaceId}/users/search/v1")
    public ResponseEntity<?> spaceRoleUserSearchV1(
            @Parameter(example = "5d956a15-6858-40ba-b0aa-b123c54e250d") @PathVariable UUID spaceId,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._DETAILED) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showSpaceRoleMode, defaultValue = SpaceRoleUserBaseDTOMapper.Mode._SHORT) SpaceRoleUserBaseDTOMapper.Mode spaceRoleMode,
            @RequestBody SpaceRoleUserSearchDTOv1 request) {
        UserWithinSpaceRolesListRsDTOv1 rs = new UserWithinSpaceRolesListRsDTOv1();
        try {
            rs.spaceRoleUserList = spaceRoleUserDTOMapper.convertList(
                    spaceUserRoleService.getUsersRefRolesMap(userSearchRqDTOReverseMapper.convert(request), spaceId), new MapperContext().setMode(showUserMode).setMode(spaceRoleMode)
            );
        } catch (ServiceException se) {
            se.printStackTrace();
            return createErrorRs(se, rs);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
