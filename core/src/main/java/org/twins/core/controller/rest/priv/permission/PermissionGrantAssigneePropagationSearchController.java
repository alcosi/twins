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
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationSearchRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationSearchRsDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantAssigneePropagationRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantAssigneePropagationSearchDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantAssigneePropagationSearchService;
import org.twins.core.service.permission.PermissionGrantAssigneePropagationService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Search permission grant assignee propagation", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_GRANT_ASSIGNEE_PROPAGATION_MANAGE, Permissions.PERMISSION_GRANT_ASSIGNEE_PROPAGATION_VIEW})
public class PermissionGrantAssigneePropagationSearchController extends ApiController {

    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PermissionGrantAssigneePropagationSearchService permissionGrantAssigneePropagationSearchService;
    private final PermissionGrantAssigneePropagationSearchDTOReverseMapper permissionGrantAssigneePropagationSearchDTOReverseMapper;
    private final PermissionGrantAssigneePropagationRestDTOMapper permissionGrantAssigneePropagationRestDTOMapper;
    private final PermissionGrantAssigneePropagationService permissionGrantAssigneePropagationService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantAssigneePropagationSearchV1", summary = "Permission grant assignee propagation search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant assignee propagation list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantAssigneePropagationSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/assignee_propagation/search/v1")
    public ResponseEntity<?> permissionGrantAssigneePropagationSearchV1(
            @MapperContextBinding(roots = PermissionGrantAssigneePropagationRestDTOMapper.class, response = PermissionGrantAssigneePropagationSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody PermissionGrantAssigneePropagationSearchRqDTOv1 request) {
        PermissionGrantAssigneePropagationSearchRsDTOv1 rs = new PermissionGrantAssigneePropagationSearchRsDTOv1();
        try {
            PaginationResult<PermissionGrantAssigneePropagationEntity> permissionGrants = permissionGrantAssigneePropagationSearchService
                    .findPermissionAssigneePropagations(permissionGrantAssigneePropagationSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setPermissionGrantAssigneePropagations(permissionGrantAssigneePropagationRestDTOMapper.convertCollection(permissionGrants.getList(), mapperContext))
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
    @Operation(operationId = "permissionGrantAssigneePropagationViewV1", summary = "Permission grant assignee propagation view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant assignee propagation", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantAssigneePropagationViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/permission_grant/assignee_propagation/{grantId}/v1")
    public ResponseEntity<?> permissionGrantAssigneePropagationViewV1(
            @MapperContextBinding(roots = PermissionGrantAssigneePropagationRestDTOMapper.class, response = PermissionGrantAssigneePropagationViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.PERMISSION_GRANT_ASSIGNEE_PROPAGATION_ID) @PathVariable("grantId") UUID grentId) {
        PermissionGrantAssigneePropagationViewRsDTOv1 rs = new PermissionGrantAssigneePropagationViewRsDTOv1();
        try {
            PermissionGrantAssigneePropagationEntity permissionGrant = permissionGrantAssigneePropagationService.findEntitySafe(grentId);

            rs
                    .setPermissionGrantAssigneePropagation(permissionGrantAssigneePropagationRestDTOMapper.convert(permissionGrant, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
