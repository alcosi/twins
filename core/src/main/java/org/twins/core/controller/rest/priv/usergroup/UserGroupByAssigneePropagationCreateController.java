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
import org.twins.core.dao.usergroup.UserGroupByAssigneePropagationEntity;
import org.twins.core.dto.rest.usergroup.UserGroupByAssigneePropagationCreateRqDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupByAssigneePropagationRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.usergroup.UserGroupByAssigneePropagationCreateDTOReverseMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupByAssigneePropagationRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.usergroup.UserGroupByAssigneePropagationService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "Create user group by assignee propagation", name = ApiTag.USER_GROUP)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.USER_GROUP_BY_ASSIGNEE_PROPAGATION_MANAGE, Permissions.USER_GROUP_BY_ASSIGNEE_PROPAGATION_CREATE})
public class UserGroupByAssigneePropagationCreateController extends ApiController {
    private final UserGroupByAssigneePropagationService userGroupByAssigneePropagationService;
    private final UserGroupByAssigneePropagationRestDTOMapper userGroupByAssigneePropagationRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final UserGroupByAssigneePropagationCreateDTOReverseMapper userGroupByAssigneePropagationCreateDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupByAssigneePropagationCreateV1", summary = "user group by assignee propagation create add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "user group by assignee propagation add", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupByAssigneePropagationRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/user_group/assignee_propagation/v1")
    public ResponseEntity<?> userGroupByAssigneePropagationCreateV1(
            @MapperContextBinding(roots = UserGroupByAssigneePropagationRestDTOMapper.class, response = UserGroupByAssigneePropagationRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody UserGroupByAssigneePropagationCreateRqDTOv1 request) {
        UserGroupByAssigneePropagationRsDTOv1 rs = new UserGroupByAssigneePropagationRsDTOv1();
        try {
            UserGroupByAssigneePropagationEntity userGroupByAssigneePropagation = userGroupByAssigneePropagationService.createUserGroupByAssigneePropagationEntity
                    (userGroupByAssigneePropagationCreateDTOReverseMapper.convert(request.getUserGroupByAssigneePropagation()));
            rs
                    .setUserGroupByAssigneePropagation(userGroupByAssigneePropagationRestDTOMapper.convert(userGroupByAssigneePropagation, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
