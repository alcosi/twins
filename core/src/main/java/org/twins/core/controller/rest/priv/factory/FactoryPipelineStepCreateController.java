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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryPipelineStepCreateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineStepSaveRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryPipelineStepCreateDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryPipelineStepRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryPipelineStepService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "", name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PIPELINE_STEP_MANAGE, Permissions.PIPELINE_STEP_CREATE})
public class FactoryPipelineStepCreateController extends ApiController {
    private final FactoryPipelineStepService factoryPipelineStepService;
    private final FactoryPipelineStepRestDTOMapper factoryPipelineStepRestDTOMapper;
    private final FactoryPipelineStepCreateDTOReverseMapper factoryPipelineStepCreateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = " factoryPipelineStepCreateV1", summary = "factory pipeline step add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "factory pipeline step added successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryPipelineStepSaveRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory/factory_pipeline/{factoryPipelineId}/factory_pipeline_step/v1")
    public ResponseEntity<?> factoryPipelineStepCreateV1(
            @MapperContextBinding(roots = FactoryPipelineStepRestDTOMapper.class, response = FactoryPipelineStepSaveRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACTORY_PIPELINE_ID) @PathVariable UUID factoryPipelineId,
            @RequestBody FactoryPipelineStepCreateRqDTOv1 request) {
        FactoryPipelineStepSaveRsDTOv1 rs = new FactoryPipelineStepSaveRsDTOv1();
        try {
            request.getFactoryPipelineStep().setFactoryPipelineId(factoryPipelineId);
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
