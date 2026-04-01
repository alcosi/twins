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
import org.twins.core.dao.usergroup.UserGroupInvolveActAsUserEntity;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveActAsUserCreateRqDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveActAsUserListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.usergroup.UserGroupInvolveActAsUserCreateDTOReverseMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupInvolveActAsUserRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.usergroup.UserGroupInvolveActAsUserService;

import java.util.List;

@Tag(description = "Create userGroup by act as user", name = ApiTag.USER_GROUP)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.USER_GROUP_INVOLVE_ACT_AS_USER_CREATE})
public class UserGroupInvolveActAsUserCreateController extends ApiController {

    private final UserGroupInvolveActAsUserService userGroupInvolveActAsUserService;
    private final UserGroupInvolveActAsUserRestDTOMapper userGroupInvolveActAsUserRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final UserGroupInvolveActAsUserCreateDTOReverseMapper userGroupInvolveActAsUserCreateDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupInvolveActAsUserCreateV1", summary = "create(batch) user group by act as user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "user group by act as user propagation add", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupInvolveActAsUserListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/user_group/involve_act_as_user/v1")
    public ResponseEntity<?> userGroupInvolveActAsUserBatchCreateV1(
            @MapperContextBinding(roots = UserGroupInvolveActAsUserRestDTOMapper.class, response = UserGroupInvolveActAsUserListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody UserGroupInvolveActAsUserCreateRqDTOv1 request) {
        UserGroupInvolveActAsUserListRsDTOv1 rs = new UserGroupInvolveActAsUserListRsDTOv1();
        try {
            List<UserGroupInvolveActAsUserEntity> entities = userGroupInvolveActAsUserCreateDTOReverseMapper.convertCollection(request.getUserGroupInvolves());
            List<UserGroupInvolveActAsUserEntity> createdList = userGroupInvolveActAsUserService.createUserGroupInvolveActAsUser(entities);
            rs
                    .setUserGroupInvolves(userGroupInvolveActAsUserRestDTOMapper.convertCollection(createdList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
