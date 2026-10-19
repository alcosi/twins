package org.twins.core.controller.rest.priv.permission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
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
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dto.rest.permission.PermissionCountRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionCountRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.permission.PermissionCountRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionSearchDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.PermissionSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "Count permissions", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PERMISSION_MANAGE, Permissions.PERMISSION_VIEW})
public class PermissionCountController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final PermissionSearchDTOReverseMapper permissionSearchDTOReverseMapper;
    private final PermissionCountRestDTOMapper permissionCountRestDTOMapper;
    private final PermissionSearchService permissionSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionCountV1", summary = "Returns permission count grouped by specified fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/permission/count/v1")
    public ResponseEntity<?> permissionCountV1(
            @MapperContextBinding(roots = PermissionCountRestDTOMapper.class, response = PermissionCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid PermissionCountRqDTOv1 request) {
        PermissionCountRsDTOv1 rs = new PermissionCountRsDTOv1();
        try {
            var results = permissionSearchService
                    .countByGroupFields(permissionSearchDTOReverseMapper.convert(request.getSearch()), request.getGroupFields(), pagination);
            rs
                    .setCounts(permissionCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(results))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
