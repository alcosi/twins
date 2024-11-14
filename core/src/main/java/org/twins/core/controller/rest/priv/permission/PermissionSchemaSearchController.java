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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dto.rest.permission.PermissionSchemaSearchRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionSchemaSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.permission.PermissionSchemaRestDTOMapperV2;
import org.twins.core.mappers.rest.permission.PermissionSchemaSearchDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionSchemaSearchService;

@Tag(name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PermissionSchemaSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final PermissionSchemaSearchService permissionSchemaSearchService;
    private final PermissionSchemaRestDTOMapperV2 permissionSchemaRestDTOMapperV2;
    private final PermissionSchemaSearchDTOReverseMapper permissionSchemaSearchDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionSchemaSearchV1", summary = "Return a list of all permission schemas for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionSchemaSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_schema/search/v1")
    public ResponseEntity<?> permissionSchemaSearchV1(
            @MapperContextBinding(roots = PermissionSchemaRestDTOMapperV2.class, response = PermissionSchemaSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody PermissionSchemaSearchRqDTOv1 request) {
        PermissionSchemaSearchRsDTOv1 rs = new PermissionSchemaSearchRsDTOv1();
        try {
            PaginationResult<PermissionSchemaEntity> permissionList = permissionSchemaSearchService
                    .findPermissionSchemasByDomain(permissionSchemaSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setPermissionSchemas(permissionSchemaRestDTOMapperV2.convertCollection(permissionList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(permissionList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
