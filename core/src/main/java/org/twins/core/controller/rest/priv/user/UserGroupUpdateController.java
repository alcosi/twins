package org.twins.core.controller.rest.priv.user;

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
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.domain.usergroup.UserGroupUpdate;
import org.twins.core.dto.rest.notification.HistoryNotificationListRsDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupListRsDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupUpdateDTOReverseMapper;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.List;

@Tag(description = "Update user group", name = ApiTag.USER_GROUP)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserGroupUpdateController extends ApiController {

    private final UserGroupUpdateDTOReverseMapper userGroupUpdateDTOReverseMapper;
    private final UserGroupService userGroupService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final UserGroupRestDTOMapper userGroupListRsDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupUpdateV1", summary = "Update batch user group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The user group batch was updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = HistoryNotificationListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/user_group/v1")
    public ResponseEntity<?> historyNotificationUpdateV1(
            @MapperContextBinding(roots = UserGroupRestDTOMapper.class, response = UserGroupListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody UserGroupUpdateRqDTOv1 request) {
        UserGroupListRsDTOv1 rs = new UserGroupListRsDTOv1();
        try {
            List<UserGroupUpdate> updateList = userGroupUpdateDTOReverseMapper.convertCollection(request.getUserGroups());
            List<UserGroupEntity> historyNotificationList = userGroupService.updateUserGroup(updateList);
            rs
                    .setUserGroupList(userGroupListRsDTOMapper.convertCollection(historyNotificationList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
