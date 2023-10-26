package org.twins.core.controller.rest.priv.usergroup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.usergroup.UserGroupListRsDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupMemberManageRqDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserGroupService;
import org.twins.core.service.user.UserService;

import java.util.UUID;

@Tag(name = ApiTag.USER_GROUP)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserGroupMemberManageController extends ApiController {
    final UserGroupRestDTOMapper userGroupDTOMapper;
    final UserGroupService userGroupService;
    final UserService userService;
    final AuthService authService;

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupMemberManageV1", summary = "Assign or discharge some group to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/user/{userId}/user_group/manage/v1", method = RequestMethod.POST)
    public ResponseEntity<?> userGroupMemberManageV1(
            @Parameter(name = "userId", in = ParameterIn.PATH, required = true, example = DTOExamples.USER_ID) @PathVariable UUID userId,
            @Parameter(name = "showUserGroupMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = UserGroupRestDTOMapper.Mode._DETAILED) UserGroupRestDTOMapper.Mode showUserGroupMode,
            @RequestBody UserGroupMemberManageRqDTOv1 request) {
        UserGroupListRsDTOv1 rs = new UserGroupListRsDTOv1();
        try {
            userGroupService.manageForUser(userService.checkUserId(userId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS), request.getUserGroupEnterList(), request.getUserGroupExitList());
            rs.userGroupList = userGroupDTOMapper.convertList(
                    userGroupService.findGroupsForUser(userId), new MapperContext().setMode(showUserGroupMode));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }


}
