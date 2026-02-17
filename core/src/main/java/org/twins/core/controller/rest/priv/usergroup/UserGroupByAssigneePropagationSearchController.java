package org.twins.core.controller.rest.priv.usergroup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.usergroup.UserGroupByAssigneePropagationEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.usergroup.UserGroupByAssigneePropagationSearchRqDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupByAssigneePropagationSearchRsDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupByAssigneePropagationViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupByAssigneePropagationRestDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupByAssigneePropagationSearchDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.usergroup.UserGroupByAssigneePropagationSearchService;
import org.twins.core.service.usergroup.UserGroupByAssigneePropagationService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Search user group by assignee propagation", name = ApiTag.USER_GROUP)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.USER_GROUP_BY_ASSIGNEE_PROPAGATION_MANAGE, Permissions.USER_GROUP_BY_ASSIGNEE_PROPAGATION_VIEW})
public class UserGroupByAssigneePropagationSearchController extends ApiController {

    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final UserGroupByAssigneePropagationSearchService userGroupByAssigneePropagationSearchService;
    private final UserGroupByAssigneePropagationSearchDTOReverseMapper userGroupByAssigneePropagationSearchDTOReverseMapper;
    private final UserGroupByAssigneePropagationRestDTOMapper userGroupByAssigneePropagationRestDTOMapper;
    private final UserGroupByAssigneePropagationService userGroupByAssigneePropagationService;

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupByAssigneePropagationSearchV1", summary = "User group by assignee propagation search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User group by assignee propagation list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupByAssigneePropagationSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/assignee_propagation/search/v1")
    public ResponseEntity<?> userGroupByAssigneePropagationSearchV1(
            @MapperContextBinding(roots = UserGroupByAssigneePropagationRestDTOMapper.class, response = UserGroupByAssigneePropagationSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody UserGroupByAssigneePropagationSearchRqDTOv1 request) {
        UserGroupByAssigneePropagationSearchRsDTOv1 rs = new UserGroupByAssigneePropagationSearchRsDTOv1();
        try {
            PaginationResult<UserGroupByAssigneePropagationEntity> permissionGrants = userGroupByAssigneePropagationSearchService
                    .findPermissionAssigneePropagations(userGroupByAssigneePropagationSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setUserGroupByAssigneePropagations(userGroupByAssigneePropagationRestDTOMapper.convertCollection(permissionGrants.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(permissionGrants))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupByAssigneePropagationViewV1", summary = "User group by assignee propagation view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User group by assignee propagation", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserGroupByAssigneePropagationViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/permission_grant/assignee_propagation/{grantId}/v1")
    public ResponseEntity<?> userGroupByAssigneePropagationViewV1(
            @MapperContextBinding(roots = UserGroupByAssigneePropagationRestDTOMapper.class, response = UserGroupByAssigneePropagationViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.USER_GROUP_BY_ASSIGNEE_PROPAGATION_ID) @PathVariable("grantId") UUID grentId) {
        UserGroupByAssigneePropagationViewRsDTOv1 rs = new UserGroupByAssigneePropagationViewRsDTOv1();
        try {
            UserGroupByAssigneePropagationEntity permissionGrant = userGroupByAssigneePropagationService.findEntitySafe(grentId);

            rs
                    .setUserGroupByAssigneePropagation(userGroupByAssigneePropagationRestDTOMapper.convert(permissionGrant, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
