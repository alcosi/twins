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
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryPipelineStepSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineStepSearchRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineStepViewRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryPipelineStepRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryPipelineStepSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryPipelineStepSearchService;
import org.twins.core.service.factory.FactoryPipelineStepService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PIPELINE_STEP_MANAGE, Permissions.PIPELINE_STEP_VIEW})
public class FactoryPipelineStepSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryPipelineStepSearchDTOReverseMapper factoryPipelineStepSearchDTOReverseMapper;
    private final FactoryPipelineStepRestDTOMapper factoryPipelineStepRestDTOMapper;
    private final FactoryPipelineStepSearchService factoryPipelineStepSearchService;
    private final FactoryPipelineStepService factoryPipelineStepService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryPipelineStepSearchV1", summary = "Factory pipeline step search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory pipeline step list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryPipelineStepSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_pipeline_step/search/v1")
    public ResponseEntity<?> factoryPipelineStepSearchV1(
            @MapperContextBinding(roots = FactoryPipelineStepRestDTOMapper.class, response = FactoryPipelineStepSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryPipelineStepSearchRqDTOv1 request) {
        FactoryPipelineStepSearchRsDTOv1 rs = new FactoryPipelineStepSearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryPipelineStepEntity> pipelineSteps = factoryPipelineStepSearchService
                    .findFactoryPipelineSteps(factoryPipelineStepSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setSteps(factoryPipelineStepRestDTOMapper.convertCollection(pipelineSteps.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(pipelineSteps))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryPipelineStepViewV1", summary = "Factory pipeline step")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory pipeline step", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryPipelineStepViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/factory_pipeline_step/{stepId}/v1")
    public ResponseEntity<?> factoryPipelineStepViewV1(
            @MapperContextBinding(roots = FactoryPipelineStepRestDTOMapper.class, response = FactoryPipelineStepViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACTORY_PIPELINE_STEP_ID) @PathVariable("stepId") UUID stepId) {
        FactoryPipelineStepViewRsDTOv1 rs = new FactoryPipelineStepViewRsDTOv1();
        try {
            TwinFactoryPipelineStepEntity pipelineStep = factoryPipelineStepService.findEntitySafe(stepId);

            rs
                    .setStep(factoryPipelineStepRestDTOMapper.convert(pipelineStep, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
