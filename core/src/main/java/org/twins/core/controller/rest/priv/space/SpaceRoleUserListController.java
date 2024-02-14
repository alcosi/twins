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
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.space.SpaceRoleUserSearchDTOv1;
import org.twins.core.dto.rest.user.UserListRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.mappers.rest.space.SpaceRoleUserSearchRqDTOMapper;
import org.twins.core.service.space.SpaceUserRoleService;

import java.util.UUID;

@Tag(name = ApiTag.SPACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class SpaceRoleUserListController extends ApiController {
    final UserRestDTOMapper userRestDTOMapper;
    final SpaceUserRoleService spaceUserRoleService;
    final SpaceRoleUserSearchRqDTOMapper userSearchRqDTOMapper;

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
            rs.userList = userRestDTOMapper.convertList(
                    spaceUserRoleService.findUserByRole(spaceId, roleId), new MapperContext().setMode(showUserMode));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "spaceRoleWithinUserListV1", summary = "Return all users of specific space")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/space/{spaceId}/users/list/v1")
    public ResponseEntity<?> spaceUserListV1(
            @Parameter(example = "5d956a15-6858-40ba-b0aa-b123c54e250d") @PathVariable UUID spaceId,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._DETAILED) UserRestDTOMapper.Mode showUserMode) {
        UserListRsDTOv1 rs = new UserListRsDTOv1();
        try {
            rs.userList = userRestDTOMapper.convertList(spaceUserRoleService.findAllUsersBySpaceId(spaceId), new MapperContext().setMode(showUserMode));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "spaceRoleWithinUserListV1", summary = "Search users within their roles of specific space")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/space/{spaceId}/users/search/v1")
    public ResponseEntity<?> spaceRoleUserSearchV1(
            @Parameter(example = "5d956a15-6858-40ba-b0aa-b123c54e250d") @PathVariable UUID spaceId,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._DETAILED) UserRestDTOMapper.Mode showUserMode,
            @RequestBody SpaceRoleUserSearchDTOv1 request) {
        UserListRsDTOv1 rs = new UserListRsDTOv1();
        try {
            rs.userList = userRestDTOMapper.convertList(spaceUserRoleService.findUsers(userSearchRqDTOMapper.convert(request), spaceId), new MapperContext().setMode(showUserMode));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
