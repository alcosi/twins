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
import org.twins.core.dao.permission.PermissionGrantTwinRoleEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionGrantTwinRoleSearchRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantTwinRoleSearchRsDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantTwinRoleViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantTwinRoleRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantTwinRoleSearchRqDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantTwinRoleSearchService;
import org.twins.core.service.permission.PermissionGrantTwinRoleService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Search permission grant twin role", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_GRANT_TWIN_ROLE_MANAGE, Permissions.PERMISSION_GRANT_TWIN_ROLE_VIEW})
public class PermissionGrantTwinRoleSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PermissionGrantTwinRoleRestDTOMapper permissionGrantTwinRoleRestDTOMapper;
    private final PermissionGrantTwinRoleSearchService permissionGrantTwinRoleSearchService;
    private final PermissionGrantTwinRoleSearchRqDTOReverseMapper permissionGrantTwinRoleSearchRqDTOReverseMapper;
    private final PermissionGrantTwinRoleService permissionGrantTwinRoleService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantTwinRoleSearchV1", summary = "Permission grant twin role search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant twin role list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantTwinRoleSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/twin_role/search/v1")
    public ResponseEntity<?> permissionGrantTwinRoleSearchV1(
            @MapperContextBinding(roots = PermissionGrantTwinRoleRestDTOMapper.class, response = PermissionGrantTwinRoleSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody PermissionGrantTwinRoleSearchRqDTOv1 request) {
        PermissionGrantTwinRoleSearchRsDTOv1 rs = new PermissionGrantTwinRoleSearchRsDTOv1();
        try {
            PaginationResult<PermissionGrantTwinRoleEntity> permissionGrantTwinRoleList = permissionGrantTwinRoleSearchService
                    .findPermissionGrantTwinRoles(permissionGrantTwinRoleSearchRqDTOReverseMapper.convert(request), pagination);
            rs
                    .setPermissionGrantTwinRoles(permissionGrantTwinRoleRestDTOMapper.convertCollection(permissionGrantTwinRoleList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(permissionGrantTwinRoleList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantTwinRoleViewV1", summary = "Permission grant twin role view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant twin role", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantTwinRoleViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/permission_grant/twin_role/{grantId}/v1")
    public ResponseEntity<?> permissionGrantTwinRoleViewV1(
            @MapperContextBinding(roots = PermissionGrantTwinRoleRestDTOMapper.class, response = PermissionGrantTwinRoleViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.PERMISSION_GRANT_TWIN_ROLE_ID) @PathVariable("grantId") UUID grantId) {
        PermissionGrantTwinRoleViewRsDTOv1 rs = new PermissionGrantTwinRoleViewRsDTOv1();
        try {
            PermissionGrantTwinRoleEntity permissionGrantTwinRole = permissionGrantTwinRoleService.findEntitySafe(grantId);

            rs
                    .setPermissionGrantTwin(permissionGrantTwinRoleRestDTOMapper.convert(permissionGrantTwinRole, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
