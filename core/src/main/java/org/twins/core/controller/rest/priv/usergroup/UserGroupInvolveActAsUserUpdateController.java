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
import org.twins.core.dao.usergroup.UserGroupInvolveActAsUserEntity;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveActAsUserListRsDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveActAsUserUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.usergroup.UserGroupInvolveActAsUserRestDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupInvolveActAsUserUpdateDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.usergroup.UserGroupInvolveActAsUserService;

import java.util.List;

@Tag(description = "Update userGroup by act as user", name = ApiTag.USER_GROUP)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.USER_GROUP_INVOLVE_ACT_AS_USER_UPDATE})
public class UserGroupInvolveActAsUserUpdateController extends ApiController {

    private final UserGroupInvolveActAsUserUpdateDTOReverseMapper userGroupInvolveActAsUserUpdateDTOReverseMapper;
    private final UserGroupInvolveActAsUserService userGroupInvolveActAsUserService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final UserGroupInvolveActAsUserRestDTOMapper userGroupInvolveActAsUserRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupInvolveActAsUserUpdateV1", summary = "Update (batch) userGroup by act as user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The usergroup by act as user batch was updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupInvolveActAsUserListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/user_group/involve_act_as_user/v1")
    public ResponseEntity<?> userGroupInvolveActAsUserBatchUpdateV1(
            @MapperContextBinding(roots = UserGroupInvolveActAsUserRestDTOMapper.class, response = UserGroupInvolveActAsUserListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody UserGroupInvolveActAsUserUpdateRqDTOv1 request) {
        UserGroupInvolveActAsUserListRsDTOv1 rs = new UserGroupInvolveActAsUserListRsDTOv1();
        try {
            List<UserGroupInvolveActAsUserEntity> updateList = userGroupInvolveActAsUserUpdateDTOReverseMapper.convertCollection(request.getUserGroupInvolves());
            List<UserGroupInvolveActAsUserEntity> updatedList = userGroupInvolveActAsUserService.updateUserGroupInvolveActAsUser(updateList);
            rs
                    .setUserGroupInvolves(userGroupInvolveActAsUserRestDTOMapper.convertCollection(updatedList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}