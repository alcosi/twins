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
import org.twins.core.dao.projection.ProjectionExclusionEntity;
import org.twins.core.dto.rest.projection.ProjectionExclusionSearchRqDTOv1;
import org.twins.core.dto.rest.projection.ProjectionExclusionSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.projection.ProjectionExclusionRestDTOMapper;
import org.twins.core.mappers.rest.projection.ProjectionExclusionSearchRestDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.projection.ProjectionExclusionSearchService;

@Tag(description = "", name = ApiTag.PROJECTION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PROJECTION_EXCLUSION_MANAGE, Permissions.PROJECTION_EXCLUSION_VIEW})
public class ProjectionExclusionSearchController extends ApiController {
    private final ProjectionExclusionRestDTOMapper projectionExclusionRestDTOMapper;
    private final ProjectionExclusionSearchService projectionExclusionSearchService;
    private final ProjectionExclusionSearchRestDTOReverseMapper projectionExclusionSearchRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "projectionExclusionSearchV1", summary = "Returns projection exclusions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projection exclusion list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ProjectionExclusionSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/projection_exclusion/search/v1")
    public ResponseEntity<?> projectionSearchV1(
            @MapperContextBinding(roots = ProjectionExclusionRestDTOMapper.class, response = ProjectionExclusionSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody ProjectionExclusionSearchRqDTOv1 request) {
        ProjectionExclusionSearchRsDTOv1 rs = new ProjectionExclusionSearchRsDTOv1();
        try {
            PaginationResult<ProjectionExclusionEntity> projectionExclusionList = projectionExclusionSearchService
                    .findProjectionExclusions(projectionExclusionSearchRestDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setProjectionExclusions(projectionExclusionRestDTOMapper.convertCollection(projectionExclusionList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(projectionExclusionList))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
