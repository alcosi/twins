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
import org.twins.core.dao.usergroup.UserGroupInvolveAssigneeEntity;
import org.twins.core.dto.rest.usergroup.UserGroupByAssigneePropagationCreateRqDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupByAssigneePropagationRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.usergroup.UserGroupInvolveAssigneeCreateDTOReverseMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupInvolveAssigneeRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.usergroup.UserGroupInvolveAssigneeService;

@Tag(description = "Create user group by assignee propagation", name = ApiTag.USER_GROUP)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.USER_GROUP_INVOLVE_ASSIGNEE_MANAGE, Permissions.USER_GROUP_INVOLVE_ASSIGNEE_CREATE})
public class UserGroupInvolveAssigneeCreateController extends ApiController {
    private final UserGroupInvolveAssigneeService userGroupInvolveAssigneeService;
    private final UserGroupInvolveAssigneeRestDTOMapper userGroupInvolveAssigneeRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final UserGroupInvolveAssigneeCreateDTOReverseMapper userGroupInvolveAssigneeCreateDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupInvolveAssigneeCreateV1", summary = "user group by assignee propagation create add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "user group by assignee propagation add", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupByAssigneePropagationRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/user_group/involve_assignee/v1")
    public ResponseEntity<?> userGroupInvolveAssigneeCreateV1(
            @MapperContextBinding(roots = UserGroupInvolveAssigneeRestDTOMapper.class, response = UserGroupByAssigneePropagationRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody UserGroupByAssigneePropagationCreateRqDTOv1 request) {
        UserGroupByAssigneePropagationRsDTOv1 rs = new UserGroupByAssigneePropagationRsDTOv1();
        try {
            UserGroupInvolveAssigneeEntity userGroupInvolveAssignee = userGroupInvolveAssigneeService.createUserGroupByAssigneePropagationEntity
                    (userGroupInvolveAssigneeCreateDTOReverseMapper.convert(request.getUserGroupByAssigneePropagation()));
            rs
                    .setUserGroupByAssigneePropagation(userGroupInvolveAssigneeRestDTOMapper.convert(userGroupInvolveAssignee, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
