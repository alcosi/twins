package org.twins.core.controller.rest.priv.usergroup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.usergroup.UserGroupListRsDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupMemberManageRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.user.UserGroupService;
import org.twins.core.service.user.UserService;

import java.util.UUID;

@Tag(name = ApiTag.USER_GROUP)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserGroupMemberManageController extends ApiController {
    private final UserGroupRestDTOMapper userGroupDTOMapper;
    private final UserGroupService userGroupService;
    private final UserService userService;

    @Value("${api.unsecured.enable}")
    private boolean apiUnsecuredEnabled;

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupMemberManageV1", summary = "Assign or discharge some group to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/user/{userId}/user_group/manage/v1")
    public ResponseEntity<?> userGroupMemberManageV1(
            @MapperContextBinding(roots = UserGroupRestDTOMapper.class, response = UserGroupListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.USER_ID) @PathVariable UUID userId,
            @RequestBody UserGroupMemberManageRqDTOv1 request) {
        UserGroupListRsDTOv1 rs = new UserGroupListRsDTOv1();
        try {
            if (!apiUnsecuredEnabled)
                throw new ServiceException(ErrorCodeCommon.FORBIDDEN);
            userGroupService.manageForUser(userService.checkId(userId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS), request.getUserGroupEnterList(), request.getUserGroupExitList());
            rs.userGroupList = userGroupDTOMapper.convertCollection(
                    userGroupService.findGroupsForUser(userId), mapperContext);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ProtectedBy({Permissions.USER_MANAGE})
    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupMemberManageV2", summary = "Assign or discharge some group to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/user/{userId}/user_group/manage/v2")
    public ResponseEntity<?> userGroupMemberManageV2(
            @MapperContextBinding(roots = UserGroupRestDTOMapper.class, response = UserGroupListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.USER_ID) @PathVariable UUID userId,
            @RequestBody UserGroupMemberManageRqDTOv1 request) {
        UserGroupListRsDTOv1 rs = new UserGroupListRsDTOv1();
        try {
            userGroupService.manageForUser(userService.checkId(userId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS), request.getUserGroupEnterList(), request.getUserGroupExitList());
            rs.userGroupList = userGroupDTOMapper.convertCollection(
                    userGroupService.findGroupsForUser(userId), mapperContext);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }


}
