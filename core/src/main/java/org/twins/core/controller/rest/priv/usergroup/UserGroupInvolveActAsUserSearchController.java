package org.twins.core.controller.rest.priv.usergroup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
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
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.usergroup.UserGroupInvolveActAsUserEntity;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveActAsUserSearchRqDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveActAsUserSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.usergroup.UserGroupInvolveActAsUserRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.usergroup.UserGroupInvolveActAsUserSearchService;

@Tag(description = "Search userGroup by act as user", name = ApiTag.USER_GROUP)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.USER_GROUP_INVOLVE_ACT_AS_USER_MANAGE, Permissions.USER_GROUP_INVOLVE_ACT_AS_USER_VIEW})
public class UserGroupInvolveActAsUserSearchController extends ApiController {

    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final UserGroupInvolveActAsUserSearchService userGroupInvolveActAsUserSearchService;
    private final UserGroupInvolveActAsUserRestDTOMapper userGroupInvolveActAsUserRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupInvolveActAsUserSearchV1", summary = "UserGroup by act as user search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UserGroup by act as user list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupInvolveActAsUserSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/user_group/involve_act_as_user/search/v1")
    public ResponseEntity<?> userGroupInvolveActAsUserSearchV1(
            @MapperContextBinding(roots = UserGroupInvolveActAsUserRestDTOMapper.class, response = UserGroupInvolveActAsUserSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody UserGroupInvolveActAsUserSearchRqDTOv1 request) {
        UserGroupInvolveActAsUserSearchRsDTOv1 rs = new UserGroupInvolveActAsUserSearchRsDTOv1();
        try {
            PaginationResult<UserGroupInvolveActAsUserEntity> permissionGrants = userGroupInvolveActAsUserSearchService
                    .findUserGroupInvolveActAsUsers(request.getSearch(), pagination);
            rs
                    .setPagination(paginationMapper.convert(permissionGrants))
                    .setUserGroupInvolves(userGroupInvolveActAsUserRestDTOMapper.convertCollection(permissionGrants.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
