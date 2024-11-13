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
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.permission.PermissionSearchRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionSearchRsDTOv2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapperV2;
import org.twins.core.mappers.rest.permission.PermissionSearchDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionSearchService;

@Tag(name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PermissionSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final PermissionSearchDTOReverseMapper permissionSearchDTOReverseMapper;
    private final PermissionRestDTOMapperV2 permissionRestDTOMapperV2;
    private final PermissionSearchService permissionSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionSearchListV1", summary = "Return a list of all permissions for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionSearchRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission/search/v1")
    public ResponseEntity<?> permissionSearchListV1(
            @MapperContextBinding(roots = PermissionRestDTOMapperV2.class, response = PermissionSearchRsDTOv2.class) MapperContext mapperContext,
            @RequestBody PermissionSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {
        PermissionSearchRsDTOv2 rs = new PermissionSearchRsDTOv2();
        try {
            PaginationResult<PermissionEntity> permissionList = permissionSearchService
                    .findPermissionForDomain(permissionSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setPermissions(permissionRestDTOMapperV2.convertCollection(permissionList.getList(), mapperContext))
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
