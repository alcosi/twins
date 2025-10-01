package org.twins.core.controller.rest.priv.projection;

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
import org.twins.core.dao.projection.ProjectionEntity;
import org.twins.core.dto.rest.projection.ProjectionSearchRqDTOv1;
import org.twins.core.dto.rest.projection.ProjectionSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.projection.ProjectionRestDTOMapper;
import org.twins.core.mappers.rest.projection.ProjectionSearchRestDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.projection.ProjectionSearchService;

@Tag(description = "", name = ApiTag.PROJECTION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PROJECTION_MANAGE, Permissions.PROJECTION_VIEW})
public class ProjectionSearchController extends ApiController {
    private final ProjectionRestDTOMapper projectionRestDTOMapper;
    private final ProjectionSearchService projectionSearchService;
    private final ProjectionSearchRestDTOReverseMapper projectionSearchRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "projectionSearchV1", summary = "Returns projections")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projection list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ProjectionSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/projection/search/v1")
    public ResponseEntity<?> projectionSearchV1(
            @MapperContextBinding(roots = ProjectionRestDTOMapper.class, response = ProjectionSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody ProjectionSearchRqDTOv1 request) {
        ProjectionSearchRsDTOv1 rs = new ProjectionSearchRsDTOv1();
        try {
            PaginationResult<ProjectionEntity> projectionList = projectionSearchService
                    .findProjections(projectionSearchRestDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setProjections(projectionRestDTOMapper.convertCollection(projectionList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(projectionList))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
