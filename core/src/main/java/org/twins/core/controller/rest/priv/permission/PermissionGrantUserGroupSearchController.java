package org.twins.core.controller.rest.priv.permission;

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
import org.springframework.web.server.ResponseStatusException;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupSearchRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupSearchRsDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantUserGroupRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantUserGroupSearchDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantUserGroupSearchService;
import org.twins.core.service.permission.PermissionGrantUserGroupService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Search permission grant user group", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_GRANT_USER_GROUP_MANAGE, Permissions.PERMISSION_GRANT_USER_GROUP_VIEW})
public class PermissionGrantUserGroupSearchController extends ApiController {

    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PermissionGrantUserGroupSearchService permissionGrantUserGroupSearchService;
    private final PermissionGrantUserGroupSearchDTOReverseMapper permissionGrantUserGroupSearchDTOReverseMapper;
    private final PermissionGrantUserGroupRestDTOMapper permissionGrantUserGroupRestDTOMapper;
    private final PermissionGrantUserGroupService permissionGrantUserGroupService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantUserGroupSearchV1", summary = "Permission grant user-group search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant user group list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantUserGroupSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/user_group/search/v1")
    public ResponseEntity<?> permissionGrantUserGroupSearchV1(
            @MapperContextBinding(roots = PermissionGrantUserGroupRestDTOMapper.class, response = PermissionGrantUserGroupSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody PermissionGrantUserGroupSearchRqDTOv1 request) {
        PermissionGrantUserGroupSearchRsDTOv1 rs = new PermissionGrantUserGroupSearchRsDTOv1();
        try {
            PaginationResult<PermissionGrantUserGroupEntity> permissionGrants = permissionGrantUserGroupSearchService
                    .findPermissionGrantUserGroups(permissionGrantUserGroupSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setPermissionGrantUserGroups(permissionGrantUserGroupRestDTOMapper.convertCollection(permissionGrants.getList(), mapperContext))
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
    @Operation(operationId = "permissionGrantUserGroupViewV1", summary = "Permission grant user-group view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant user group", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantUserGroupViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/permission_grant/user_group/{grantId}/v1")
    public ResponseEntity<?> permissionGrantUserGroupViewV1(
            @MapperContextBinding(roots = PermissionGrantUserGroupRestDTOMapper.class, response = PermissionGrantUserGroupViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.PERMISSION_GRANT_USER_GROUP_ID) @PathVariable("grantId") UUID grantId) {
        PermissionGrantUserGroupViewRsDTOv1 rs = new PermissionGrantUserGroupViewRsDTOv1();
        try {
            PermissionGrantUserGroupEntity permissionGrant = permissionGrantUserGroupService.findEntitySafe(grantId);
            if (permissionGrant == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such grant user group: " + grantId + " in current domain.");
            }
            rs
                    .setPermissionGrantUserGroup(permissionGrantUserGroupRestDTOMapper.convert(permissionGrant, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
