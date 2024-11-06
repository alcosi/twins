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
import org.twins.core.dao.permission.PermissionSchemaUserGroupEntity;
import org.twins.core.dto.rest.permission.PermissionSchemaUserGroupSearchRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionSchemaUserGroupSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.permission.PermissionSchemaSearchRqDTOReverseMapper;
import org.twins.core.mappers.rest.permission.PermissionSchemaUserGroupRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionSchemaSearchService;

@Tag(name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PermissionSchemaUserGroupSearchController extends ApiController {

    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PermissionSchemaSearchService permissionSchemaSearchService;
    private final PermissionSchemaSearchRqDTOReverseMapper permissionSchemaSearchRqDTOReverseMapper;
    private final PermissionSchemaUserGroupRestDTOMapper permissionSchemaUserGroupRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionSchemaUserGroupSearchV1", summary = "Permission schema user group search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission schema user group list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionSchemaUserGroupSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_schema/user_group/search/v1")
    public ResponseEntity<?> permissionSchemaUserGroupSearchV1(
            @MapperContextBinding(roots = PermissionSchemaUserGroupRestDTOMapper.class, response = PermissionSchemaUserGroupSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody PermissionSchemaUserGroupSearchRqDTOv1 request) {
        PermissionSchemaUserGroupSearchRsDTOv1 rs = new PermissionSchemaUserGroupSearchRsDTOv1();
        try {
            PaginationResult<PermissionSchemaUserGroupEntity> permissionSchemas = permissionSchemaSearchService
                    .findPermissionSchemas(permissionSchemaSearchRqDTOReverseMapper.convert(request), pagination);
            rs
                    .setPermissionSchemaUserGroup(permissionSchemaUserGroupRestDTOMapper.convertCollection(permissionSchemas.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(permissionSchemas))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
