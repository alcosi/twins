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
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionGrantUserSearchRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantUserSearchRsDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantUserViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantUserRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantUserSearchDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantUserSearchService;
import org.twins.core.service.permission.PermissionGrantUserService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Search permission grant user", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_GRANT_USER_MANAGE, Permissions.PERMISSION_GRANT_USER_VIEW})
public class PermissionGrantUserSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final PermissionGrantUserSearchService permissionGrantUserSearchService;
    private final PermissionGrantUserRestDTOMapper permissionGrantUserRestDTOMapper;
    private final PermissionGrantUserSearchDTOReverseMapper permissionGrantUserSearchDTOReverseMapper;
    private final PermissionGrantUserService permissionGrantUserService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantUserSearchV1", summary = "Return a list of all permission grant users for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantUserSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/user/search/v1")
    public ResponseEntity<?> permissionGrantUserSearchV1(
            @MapperContextBinding(roots = PermissionGrantUserRestDTOMapper.class, response = PermissionGrantUserSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody PermissionGrantUserSearchRqDTOv1 request) {
        PermissionGrantUserSearchRsDTOv1 rs = new PermissionGrantUserSearchRsDTOv1();
        try {
            PaginationResult<PermissionGrantUserEntity> permissionGrants = permissionGrantUserSearchService
                    .findPermissionGrantUsersByDomain(permissionGrantUserSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setPermissionGrantUsers(permissionGrantUserRestDTOMapper.convertCollection(permissionGrants.getList(), mapperContext))
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
    @Operation(operationId = "permissionGrantUserViewV1", summary = "Return the permission grant users for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantUserViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/user/{grantId}/v1")
    public ResponseEntity<?> permissionGrantUserViewV1(
            @MapperContextBinding(roots = PermissionGrantUserRestDTOMapper.class, response = PermissionGrantUserViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.PERMISSION_GRANT_USER_ID) @PathVariable("grantId") UUID grantId) {

        PermissionGrantUserViewRsDTOv1 rs = new PermissionGrantUserViewRsDTOv1();
        try {
            PermissionGrantUserEntity permissionGrant = permissionGrantUserService.findEntitySafe(grantId);

            rs
                    .setPermissionGrantUser(permissionGrantUserRestDTOMapper.convert(permissionGrant, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
