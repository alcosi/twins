package org.twins.core.controller.rest.priv.factory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineStepCreateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineStepListRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryPipelineStepCreateDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryPipelineStepRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryPipelineStepService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(description = "", name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.FACTORY_PIPELINE_STEP_CREATE)
public class FactoryPipelineStepCreateController extends ApiController {
    private final FactoryPipelineStepService factoryPipelineStepService;
    private final FactoryPipelineStepRestDTOMapper factoryPipelineStepRestDTOMapper;
    private final FactoryPipelineStepCreateDTOReverseMapper factoryPipelineStepCreateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryPipelineStepCreateV1", summary = "factory pipeline step batch add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "factory pipeline step batch added successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryPipelineStepListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory/factory_pipeline_step/v1")
    public ResponseEntity<?> factoryPipelineStepCreateV1(
            @MapperContextBinding(roots = FactoryPipelineStepRestDTOMapper.class, response = FactoryPipelineStepListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody @Valid FactoryPipelineStepCreateRqDTOv1 request) {
        FactoryPipelineStepListRsDTOv1 rs = new FactoryPipelineStepListRsDTOv1();
        try {
            List<TwinFactoryPipelineStepEntity> factoryPipelineSteps = factoryPipelineStepService.createFactoryPipelineSteps(
                    factoryPipelineStepCreateDTOReverseMapper.convertCollection(request.getFactoryPipelineSteps()));
            rs
                    .setSteps(factoryPipelineStepRestDTOMapper.convertCollection(factoryPipelineSteps, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
