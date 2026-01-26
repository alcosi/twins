package org.twins.core.controller.rest.priv.factory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryPipelineSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineSearchRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineViewRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryPipelineRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryPipelineSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryPipelineSearchService;
import org.twins.core.service.factory.FactoryPipelineService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PIPELINE_MANAGE, Permissions.PIPELINE_VIEW})
public class FactoryPipelineSearchController extends ApiController {

    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryPipelineSearchService factoryPipelineSearchService;
    private final FactoryPipelineRestDTOMapper factoryPipelineRestDTOMapper;
    private final FactoryPipelineSearchDTOReverseMapper factoryPipelineSearchDTOReverseMapper;
    private final FactoryPipelineService factoryPipelineService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryPipelineSearchV1", summary = "Factory pipeline search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory pipeline list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryPipelineSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_pipeline/search/v1")
    public ResponseEntity<?> factoryPipelineSearchV1(
            @MapperContextBinding(roots = FactoryPipelineRestDTOMapper.class, response = FactoryPipelineSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryPipelineSearchRqDTOv1 request) {
        FactoryPipelineSearchRsDTOv1 rs = new FactoryPipelineSearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryPipelineEntity> pipelines = factoryPipelineSearchService
                    .findFactoryPipelines(factoryPipelineSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setPipelines(factoryPipelineRestDTOMapper.convertCollection(pipelines.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(pipelines))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryPipelineViewV1", summary = "Factory pipeline view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory pipeline data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryPipelineViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/factory_pipeline/{pipelineId}/v1")
    public ResponseEntity<?> factoryPipelineViewV1(
            @MapperContextBinding(roots = FactoryPipelineRestDTOMapper.class, response = FactoryPipelineViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACTORY_PIPELINE_ID) @PathVariable("pipelineId") UUID pipelineId) {
        FactoryPipelineViewRsDTOv1 rs = new FactoryPipelineViewRsDTOv1();
        try {
            TwinFactoryPipelineEntity pipeline = factoryPipelineService.findEntitySafe(pipelineId);
            rs
                    .setPipeline(factoryPipelineRestDTOMapper.convert(pipeline, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
