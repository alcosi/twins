package org.twins.core.controller.rest.priv.permission;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupSearchRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantUserGroupRestDTOMapperV2;
import org.twins.core.mappers.rest.permission.PermissionGrantUserGroupSearchRqDTOReverseMapper;
import org.twins.core.mappers.rest.permission.PermissionGrantUserGroupRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantUserGroupSearchService;

@Tag(name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PermissionGrantUserGroupSearchController extends ApiController {

    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PermissionGrantUserGroupSearchService permissionGrantUserGroupSearchService;
    private final PermissionGrantUserGroupSearchRqDTOReverseMapper permissionGrantUserGroupSearchRqDTOReverseMapper;
    private final PermissionGrantUserGroupRestDTOMapperV2 permissionGrantUserGroupRestDTOMapperV2;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantUserGroupSearchV1", summary = "Permission grant user-group search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant user group list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantUserGroupSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/user_group/search/v1")
    public ResponseEntity<?> permissionGrantUserGroupSearchV1(
            @MapperContextBinding(roots = PermissionGrantUserGroupRestDTOMapperV2.class, response = PermissionGrantUserGroupSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody PermissionGrantUserGroupSearchRqDTOv1 request) {
        PermissionGrantUserGroupSearchRsDTOv1 rs = new PermissionGrantUserGroupSearchRsDTOv1();
        try {
            PaginationResult<PermissionGrantUserGroupEntity> permissionGrants = permissionGrantUserGroupSearchService
                    .findPermissionGrantUserGroups(permissionGrantUserGroupSearchRqDTOReverseMapper.convert(request), pagination);
            rs
                    .setPermissionGrantUserGroups(permissionGrantUserGroupRestDTOMapperV2.convertCollection(permissionGrants.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(permissionGrants))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
