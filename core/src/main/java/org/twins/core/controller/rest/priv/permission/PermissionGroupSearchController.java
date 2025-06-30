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
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionGroupSearchRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionGroupSearchRsDTOv1;
import org.twins.core.dto.rest.permission.PermissionGroupViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.permission.PermissionGroupRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionGroupSearchDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGroupSearchService;
import org.twins.core.service.permission.PermissionGroupService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Search permission group", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_GROUP_MANAGE, Permissions.PERMISSION_GROUP_VIEW})
public class PermissionGroupSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final PermissionGroupSearchDTOReverseMapper permissionGroupSearchDTOReverseMapper;
    private final PermissionGroupRestDTOMapper permissionGroupRestDTOMapper;
    private final PermissionGroupSearchService permissionGroupSearchService;
    private final PermissionGroupService permissionGroupService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGroupSearchListV1", summary = "Return a list of all permission groups for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGroupSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_group/search/v1")
    public ResponseEntity<?> permissionGroupSearchListV1(
            @MapperContextBinding(roots = PermissionGroupRestDTOMapper.class, response = PermissionGroupSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody PermissionGroupSearchRqDTOv1 request) {
        PermissionGroupSearchRsDTOv1 rs = new PermissionGroupSearchRsDTOv1();
        try {
            PaginationResult<PermissionGroupEntity> permissionList = permissionGroupSearchService
                    .findPermissionGroupForDomain(permissionGroupSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setPermissionGroups(permissionGroupRestDTOMapper.convertCollection(permissionList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(permissionList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGroupViewV1", summary = "Return the permission group by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGroupViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/permission_group/{groupId}/v1")
    public ResponseEntity<?> permissionGroupViewV1(
            @MapperContextBinding(roots = PermissionGroupRestDTOMapper.class, response = PermissionGroupViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.PERMISSION_GROUP_ID) @PathVariable("groupId") UUID groupId) {
        PermissionGroupViewRsDTOv1 rs = new PermissionGroupViewRsDTOv1();
        try {
            PermissionGroupEntity permission = permissionGroupService.findEntitySafe(groupId);
            if (permission == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such permission group: " + groupId + " in current domain.");
            }
            rs
                    .setPermissionGroup(permissionGroupRestDTOMapper.convert(permission, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGroupViewByKeyV1", summary = "Return the permission group by key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGroupViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/permission_group_by_key/{groupKey}/v1")
    public ResponseEntity<?> permissionGroupViewByKeyV1(
            @MapperContextBinding(roots = PermissionGroupRestDTOMapper.class, response = PermissionGroupViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.PERMISSION_GROUP_KEY) @PathVariable("groupKey") String groupKey) {
        PermissionGroupViewRsDTOv1 rs = new PermissionGroupViewRsDTOv1();
        try {
            PermissionGroupEntity permission = permissionGroupService.findEntitySafe(groupKey);
            rs
                    .setPermissionGroup(permissionGroupRestDTOMapper.convert(permission, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
