package org.twins.core.controller.rest.priv.usergroup;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.domain.usergroup.UserGroupCreate;
import org.twins.core.dto.rest.usergroup.UserGroupCreateRqDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveAssigneeRsDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.usergroup.UserGroupCreateDTOReverseMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.List;

@Tag(description = "Create user groups", name = ApiTag.USER_GROUP)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.USER_GROUP_MANAGE, Permissions.USER_GROUP_CREATE})
public class UserGroupCreateController extends ApiController {

    private final UserGroupService userGroupService;
    private final UserGroupRestDTOMapper userGroupDTOMapper;
    private final UserGroupCreateDTOReverseMapper userGroupCreateDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupCreateV1", summary = "create user group(batch)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "add user group", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupInvolveAssigneeRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/user_group/v1")
    public ResponseEntity<?> userGroupBatchCreateV1(
            @MapperContextBinding(roots = UserGroupRestDTOMapper.class, response = UserGroupListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody UserGroupCreateRqDTOv1 request) {
        UserGroupListRsDTOv1 rs = new UserGroupListRsDTOv1();
        try {
            List<UserGroupCreate> createList = userGroupCreateDTOReverseMapper.convertCollection(request.getUserGroups());
            List<UserGroupEntity> userGroupList = userGroupService.createUserGroup(createList);
            rs.setUserGroupList(userGroupDTOMapper.convertCollection(userGroupList, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
