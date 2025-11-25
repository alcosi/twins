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
import org.twins.core.dao.permission.PermissionGrantSpaceRoleEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleSearchRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleSearchRsDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantRoleSpaceSearchDTOReverseMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantSpaceRoleRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantSpaceRoleSearchService;
import org.twins.core.service.permission.PermissionGrantSpaceRoleService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Search permission grant space role", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_GRANT_SPACE_ROLE_MANAGE, Permissions.PERMISSION_GRANT_SPACE_ROLE_VIEW})
public class PermissionGrantSpaceRoleSearchController extends ApiController {

    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PermissionGrantSpaceRoleSearchService permissionGrantSpaceRoleSearchService;
    private final PermissionGrantRoleSpaceSearchDTOReverseMapper permissionGrantRoleSpaceSearchDTOReverseMapper;
    private final PermissionGrantSpaceRoleRestDTOMapper permissionGrantSpaceRoleRestDTOMapper;
    private final PermissionGrantSpaceRoleService permissionGrantSpaceRoleService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantSpaceRoleSearchV1", summary = "Permission grant space role search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant space role list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantSpaceRoleSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/space_role/search/v1")
    public ResponseEntity<?> permissionGrantSpaceRoleSearchV1(
            @MapperContextBinding(roots = PermissionGrantSpaceRoleRestDTOMapper.class, response = PermissionGrantSpaceRoleSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody PermissionGrantSpaceRoleSearchRqDTOv1 request) {
        PermissionGrantSpaceRoleSearchRsDTOv1 rs = new PermissionGrantSpaceRoleSearchRsDTOv1();
        try {
            PaginationResult<PermissionGrantSpaceRoleEntity> permissionGrants = permissionGrantSpaceRoleSearchService
                    .findPermissionGrantSpaceRoles(permissionGrantRoleSpaceSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setPermissionGrantSpaceRoles(permissionGrantSpaceRoleRestDTOMapper.convertCollection(permissionGrants.getList(), mapperContext))
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
    @Operation(operationId = "permissionGrantSpaceRoleViewV1", summary = "Permission grant space role view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant space role", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantSpaceRoleViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/permission_grant/space_role/{grantId}/v1")
    public ResponseEntity<?> permissionGrantSpaceRoleViewV1(
            @MapperContextBinding(roots = PermissionGrantSpaceRoleRestDTOMapper.class, response = PermissionGrantSpaceRoleViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.SPACE_ROLE_USER_ID) @PathVariable("grantId") UUID grantId) {
        PermissionGrantSpaceRoleViewRsDTOv1 rs = new PermissionGrantSpaceRoleViewRsDTOv1();
        try {
            PermissionGrantSpaceRoleEntity role = permissionGrantSpaceRoleService.findEntitySafe(grantId);

            rs
                    .setPermissionGrantSpaceRole(permissionGrantSpaceRoleRestDTOMapper.convert(role, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
