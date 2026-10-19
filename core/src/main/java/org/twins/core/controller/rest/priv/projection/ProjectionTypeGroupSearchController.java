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
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeGroupSearchRqDTOv1;
import org.twins.core.dto.rest.projection.ProjectionTypeGroupSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.projection.ProjectionTypeGroupRestDTOMapper;
import org.twins.core.mappers.rest.projection.ProjectionTypeGroupSearchDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.projection.ProjectionTypeGroupSearchService;

@Tag(name = ApiTag.PROJECTION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PROJECTION_MANAGE, Permissions.PROJECTION_VIEW})
public class ProjectionTypeGroupSearchController extends ApiController {
    private final ProjectionTypeGroupRestDTOMapper projectionTypeGroupRestDTOMapper;
    private final ProjectionTypeGroupSearchDTOReverseMapper projectionTypeGroupSearchDTOReverseMapper;
    private final ProjectionTypeGroupSearchService projectionTypeGroupSearchService;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "projectionTypeGroupSearchV1", summary = "Projection type group search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projection type group data result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ProjectionTypeGroupSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/projection_type_group/search/v1")
    public ResponseEntity<?> projectionTypeGroupSearchV1(
            @MapperContextBinding(roots = ProjectionTypeGroupRestDTOMapper.class, response = ProjectionTypeGroupSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody ProjectionTypeGroupSearchRqDTOv1 request) {
        ProjectionTypeGroupSearchRsDTOv1 rs = new ProjectionTypeGroupSearchRsDTOv1();
        try {
            PaginationResult<ProjectionTypeGroupEntity> projectionTypeGroups = projectionTypeGroupSearchService
                    .search(projectionTypeGroupSearchDTOReverseMapper.convert(request.getSearch(), mapperContext), pagination, request.getSortField(), request.getSortDirection());
            rs
                    .setPagination(paginationMapper.convert(projectionTypeGroups))
                    .setProjectionTypeGroups(projectionTypeGroupRestDTOMapper.convertCollection(projectionTypeGroups.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
