package org.twins.core.controller.rest.priv.resource;

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
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dto.rest.resource.ResourceSearchRqDTOv1;
import org.twins.core.dto.rest.resource.ResourceSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.resource.ResourceRestDTOMapper;
import org.twins.core.mappers.rest.resource.ResourceSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.resource.ResourceSearchService;

/**
 * Lists files uploaded through {@link ResourceCreateController} of the caller's domain
 * (DomainId header, standard ApiUser flow). Guarded by RESOURCE_MANAGE or RESOURCE_VIEW;
 * the listing contains file metadata only, never the storage keys.
 */
@Tag(name = ApiTag.RESOURCE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.RESOURCE_MANAGE, Permissions.RESOURCE_VIEW})
public class ResourceSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final ResourceSearchService resourceSearchService;
    private final ResourceSearchRestDTOReverseMapper resourceSearchRestDTOReverseMapper;
    private final ResourceRestDTOMapper resourceRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "resourceSearchV1", summary = "Domain resources search",
            description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uploaded resources", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ResourceSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/resource/search/v1")
    public ResponseEntity<?> resourceSearchV1(
            @MapperContextBinding(roots = ResourceRestDTOMapper.class, response = ResourceSearchRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody ResourceSearchRqDTOv1 request) {
        ResourceSearchRsDTOv1 rs = new ResourceSearchRsDTOv1();
        try {
            PaginationResult<ResourceEntity> resourcesList = resourceSearchService
                    .search(resourceSearchRestDTOReverseMapper.convert(request.getSearch(), mapperContext), pagination, request.getSortField(), request.getSortDirection());
            rs
                    .setPagination(paginationMapper.convert(resourcesList))
                    .setResources(resourceRestDTOMapper.convertCollection(resourcesList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
