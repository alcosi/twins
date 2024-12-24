package org.twins.core.controller.rest.priv.factory;

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
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineStepSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineStepSearchRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryPipelineStepRestDTOMapperV2;
import org.twins.core.mappers.rest.factory.FactoryPipelineStepSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryPipelineStepSearchService;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FactoryPipelineStepSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryPipelineStepSearchDTOReverseMapper factoryPipelineStepSearchDTOReverseMapper;
    private final FactoryPipelineStepRestDTOMapperV2 factoryPipelineStepRestDTOMapperV2;
    private final FactoryPipelineStepSearchService factoryPipelineStepSearchService;


    @ParametersApiUserHeaders
    @Operation(operationId = "factoryPipelineStepSearchV1", summary = "Factory pipeline step search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory pipeline step list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryPipelineStepSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_pipeline_step/search/v1")
    public ResponseEntity<?> factoryPipelineStepSearchV1(
            @MapperContextBinding(roots = FactoryPipelineStepRestDTOMapperV2.class, response = FactoryPipelineStepSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryPipelineStepSearchRqDTOv1 request) {
        FactoryPipelineStepSearchRsDTOv1 rs = new FactoryPipelineStepSearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryPipelineStepEntity> pipelineSteps = factoryPipelineStepSearchService
                    .findFactoryPipelineSteps(factoryPipelineStepSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setSteps(factoryPipelineStepRestDTOMapperV2.convertCollection(pipelineSteps.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(pipelineSteps))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
