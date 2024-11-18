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
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserSearchRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantUserSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.permission.*;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionGrantyUserSearchService;

@Tag(name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PermissionGrantUserSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final PermissionGrantyUserSearchService permissionGrantyUserSearchService;
    private final PermissionGrantUserRestDTOMapperV2 permissionGrantUserRestDTOMapperV2;
    private final PermissionGrantUserSearchDTOReverseMapper permissionGrantUserSearchDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantUserSearchV1", summary = "Return a list of all permission grant users for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGrantUserSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission_grant/user/search/v1")
    public ResponseEntity<?> permissionGrantUserSearchV1(
            @MapperContextBinding(roots = PermissionGrantUserRestDTOMapperV2.class, response = PermissionGrantUserSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody PermissionGrantUserSearchRqDTOv1 request) {
        PermissionGrantUserSearchRsDTOv1 rs = new PermissionGrantUserSearchRsDTOv1();
        try {
            PaginationResult<PermissionGrantUserEntity> permissionList = permissionGrantyUserSearchService
                    .findPermissionGrantUsersByDomain(permissionGrantUserSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setPermissionGrantUsers(permissionGrantUserRestDTOMapperV2.convertCollection(permissionList.getList(), mapperContext))
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
