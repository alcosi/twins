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
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionSearchRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionSearchRsDTOv1;
import org.twins.core.dto.rest.permission.PermissionViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionSearchDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionSearchService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Search permissions", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_MANAGE, Permissions.PERMISSION_VIEW})
public class PermissionSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final PermissionSearchDTOReverseMapper permissionSearchDTOReverseMapper;
    private final PermissionRestDTOMapper permissionRestDTOMapper;
    private final PermissionSearchService permissionSearchService;
    private final PermissionService permissionService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionSearchListV1", summary = "Return a list of all permissions for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission/search/v1")
    public ResponseEntity<?> permissionSearchListV1(
            @MapperContextBinding(roots = PermissionRestDTOMapper.class, response = PermissionSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody PermissionSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {
        PermissionSearchRsDTOv1 rs = new PermissionSearchRsDTOv1();
        try {
            PaginationResult<PermissionEntity> permissionList = permissionSearchService
                    .findPermissionForDomain(permissionSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setPermissions(permissionRestDTOMapper.convertCollection(permissionList.getList(), mapperContext))
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
    @Operation(operationId = "permissionViewV1", summary = "Return the permission for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/permission/{permissionId}/v1")
    public ResponseEntity<?> permissionViewV1(
            @MapperContextBinding(roots = PermissionRestDTOMapper.class, response = PermissionViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.PERMISSION_ID) @PathVariable("permissionId") UUID permissionId) {
        PermissionViewRsDTOv1 rs = new PermissionViewRsDTOv1();
        try {
            PermissionEntity permission = permissionService.findEntitySafe(permissionId);
            rs
                    .setPermission(permissionRestDTOMapper.convert(permission, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionViewByKeyV1", summary = "Return the permission for the current domain by key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/permission_by_key/{permissionKey}/v1")
    public ResponseEntity<?> permissionViewByKeyV1(
            @MapperContextBinding(roots = PermissionRestDTOMapper.class, response = PermissionViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.PERMISSION_KEY) @PathVariable("permissionKey") String permissionKey) {
        PermissionViewRsDTOv1 rs = new PermissionViewRsDTOv1();
        try {
            PermissionEntity permission = permissionService.findEntitySafe(permissionKey);
            rs
                    .setPermission(permissionRestDTOMapper.convert(permission, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
