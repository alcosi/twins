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
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.space.SpaceRoleUserListRsDTOv1;
import org.twins.core.dto.rest.space.SpaceRoleUserRqDTOv1;
import org.twins.core.dto.rest.user.UserListRsDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupListRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.space.SpaceUserRoleService;

import java.util.UUID;

@Tag(name = ApiTag.SPACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class SpaceRoleUserListController extends ApiController {
    final UserRestDTOMapper userRestDTOMapper;
    final SpaceUserRoleService spaceUserRoleService;
    final AuthService authService;


    @ParametersApiUserHeaders
    @Operation(operationId = "spaceRoleByUserListV1", summary = "Returns user list by selected space and role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/space/{spaceId}/role/{roleId}/users/v1", method = RequestMethod.GET)
    public ResponseEntity<?> spaceRoleForUserListV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID spaceId,
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
    @Operation(operationId = "spaceRoleUserV1", summary = "Adding/removing a user to the project by role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/space/{spaceId}/role/{roleId}/users/manage/v1", method = RequestMethod.POST)
    public ResponseEntity<?> spaceRoleUserManagmentV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID spaceId,
            @Parameter(example = DTOExamples.ROLE_ID) @PathVariable UUID roleId,
            @RequestBody SpaceRoleUserRqDTOv1 request) {
        SpaceRoleUserListRsDTOv1 rs = new SpaceRoleUserListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            UUID createByUserId = apiUser.getUser().getId();
            spaceUserRoleService.manageForRoleUser(spaceId, roleId, createByUserId, request);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
