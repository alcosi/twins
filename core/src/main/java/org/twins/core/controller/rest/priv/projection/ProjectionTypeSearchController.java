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
import org.twins.core.controller.rest.annotation.*;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeSearchRsDTOv1;
import org.twins.core.dto.rest.projection.ProjectionTypeSearchRqDTOv1;
import org.twins.core.mappers.rest.projection.ProjectionTypeRestDTOMapper;
import org.twins.core.mappers.rest.projection.ProjectionTypeSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.projection.ProjectionTypeSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.PROJECTION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PROJECTION_MANAGE, Permissions.PROJECTION_VIEW})
public class ProjectionTypeSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final ProjectionTypeSearchService projectionTypeSearchService;
    private final ProjectionTypeSearchDTOReverseMapper projectionTypeSearchDTOReverseMapper;
    private final ProjectionTypeRestDTOMapper projectionTypeRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "projectionTypeSearchV1", summary = "Returns projection types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projection types prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ProjectionTypeSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/projection_type/search/v1")
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> projectionTypeSearchV1(
            @MapperContextBinding(roots = ProjectionTypeRestDTOMapper.class, response = ProjectionTypeSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody ProjectionTypeSearchRqDTOv1 request) {
        ProjectionTypeSearchRsDTOv1 rs = new ProjectionTypeSearchRsDTOv1();
        try {
            PaginationResult<ProjectionTypeEntity> projectionTypesList = projectionTypeSearchService.findProjectionTypes(projectionTypeSearchDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setPagination(paginationMapper.convert(projectionTypesList))
                    .setProjectionTypes(projectionTypeRestDTOMapper.convertCollection(projectionTypesList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
