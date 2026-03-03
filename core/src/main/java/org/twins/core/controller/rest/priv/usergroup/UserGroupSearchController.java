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
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dto.rest.usergroup.UserGroupSearchRqDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapperV2;
import org.twins.core.mappers.rest.usergroup.UserGroupSearchDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.user.UserGroupSearchService;

@Tag(name = ApiTag.USER_GROUP)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.USER_GROUP_MANAGE, Permissions.USER_GROUP_VIEW})
public class UserGroupSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final UserGroupSearchDTOReverseMapper userGroupSearchDTOReverseMapper;
    private final UserGroupSearchService userGroupSearchService;
    private final UserGroupRestDTOMapperV2 userGroupRestDTOMapperV2;

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupSearchV1", summary = "Return a list of all user groups for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/user_group/search/v1")
    public ResponseEntity<?> userGroupSearchV1(
            @MapperContextBinding(roots = UserGroupRestDTOMapperV2.class, response = UserGroupSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody UserGroupSearchRqDTOv1 request) {
        UserGroupSearchRsDTOv1 rs = new UserGroupSearchRsDTOv1();
        try {
            PaginationResult<UserGroupEntity> userGroupList = userGroupSearchService
                    .findUserGroupsForDomain(userGroupSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setUserGroupList(userGroupRestDTOMapperV2.convertCollection(userGroupList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(userGroupList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
