package org.twins.core.controller.rest.priv.factory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
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
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineStepCreateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineStepRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryPipelineStepCreateDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryPipelineStepRestDTOMapperV2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryPipelineStepService;

@Tag(description = "", name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FactoryPipelineStepCreateController extends ApiController {
    private final FactoryPipelineStepService factoryPipelineStepService;
    private final FactoryPipelineStepRestDTOMapperV2 factoryPipelineStepRestDTOMapper;
    private final FactoryPipelineStepCreateDTOReverseMapper factoryPipelineStepCreateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = " factoryPipelineStepCreateV1", summary = "factory pipeline step add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "factory pipeline step add", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryPipelineStepRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory/factory_pipeline/{factoryPipelineId}/factory_pipeline_step/v")
    public ResponseEntity<?> factoryPipelineStepCreateV1(
            @MapperContextBinding(roots = FactoryPipelineStepRestDTOMapperV2.class, response = FactoryPipelineStepRsDTOv1.class) MapperContext mapperContext,
            @RequestBody FactoryPipelineStepCreateRqDTOv1 request) {
        FactoryPipelineStepRsDTOv1 rs = new FactoryPipelineStepRsDTOv1();
        try {
            TwinFactoryPipelineStepEntity factoryPipelineStep = factoryPipelineStepService.createFactoryPipelineStep
                    (factoryPipelineStepCreateDTOReverseMapper.convert(request.getFactoryPipelineStep()));
            rs
                    .setFactoryPipelineStep(factoryPipelineStepRestDTOMapper.convert(factoryPipelineStep, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
