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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.usergroup.UserGroupInvolveAssigneeEntity;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveAssigneeListRsDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveAssigneeUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.usergroup.UserGroupInvolveAssigneeRestDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupInvolveAssigneeUpdateDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.usergroup.UserGroupInvolveAssigneeService;

import java.util.List;

@Tag(description = "Update user group by assignee propagation", name = ApiTag.USER_GROUP)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.USER_GROUP_INVOLVE_ASSIGNEE_MANAGE, Permissions.USER_GROUP_INVOLVE_ASSIGNEE_UPDATE})
public class UserGroupInvolveAssigneeUpdateController extends ApiController {
    private final UserGroupInvolveAssigneeService userGroupInvolveAssigneeService;
    private final UserGroupInvolveAssigneeRestDTOMapper userGroupInvolveAssigneeRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final UserGroupInvolveAssigneeUpdateDTOReverseMapper userGroupInvolveAssigneeUpdateDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupInvolveAssigneeUpdateV1", summary = "User group by assignee propagation update(batch)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User group by assignee propagation update", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupInvolveAssigneeListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/user_group/involve_assignee/v1")
    public ResponseEntity<?> userGroupInvolveAssigneeUpdateV1(
            @MapperContextBinding(roots = UserGroupInvolveAssigneeRestDTOMapper.class, response = UserGroupInvolveAssigneeListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody UserGroupInvolveAssigneeUpdateRqDTOv1 request) {

        UserGroupInvolveAssigneeListRsDTOv1 rs = new UserGroupInvolveAssigneeListRsDTOv1();
        try {
            List<UserGroupInvolveAssigneeEntity> userGroupInvolveAssigneeList = userGroupInvolveAssigneeService.updateUserGroupInvolveAssignee(
                    userGroupInvolveAssigneeUpdateDTOReverseMapper.convertCollection(request.getUserGroupInvolveAssignees()));
            rs
                    .setUserGroupInvolveAssigneeList(userGroupInvolveAssigneeRestDTOMapper.convertCollection(userGroupInvolveAssigneeList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
