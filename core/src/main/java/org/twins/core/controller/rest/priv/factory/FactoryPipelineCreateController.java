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
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryPipelineCreateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryPipelineCreateDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryPipelineRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryPipelineService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PIPELINE_MANAGE, Permissions.PIPELINE_CREATE})
public class FactoryPipelineCreateController extends ApiController {
    private final FactoryPipelineRestDTOMapper factoryPipelineRestDTOMapper;
    private final FactoryPipelineCreateDTOReverseMapper factoryPipelineCreateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final FactoryPipelineService factoryPipelineService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryPipelineCreateV1", summary = "Factory pipeline add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory pipeline add", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryPipelineRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory/{factoryId}/factory_pipeline/v1")
    public ResponseEntity<?> factoryPipelineCreateV1(
            @MapperContextBinding(roots = FactoryPipelineRestDTOMapper.class, response = FactoryPipelineRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACTORY_ID) @PathVariable UUID factoryId,
            @RequestBody FactoryPipelineCreateRqDTOv1 request) {
        FactoryPipelineRsDTOv1 rs = new FactoryPipelineRsDTOv1();
        try {
            TwinFactoryPipelineEntity factoryPipeline = factoryPipelineCreateDTOReverseMapper.convert(request)
                    .setTwinFactoryId(factoryId);
            factoryPipeline = factoryPipelineService.createFactoryPipeline(factoryPipeline);
            rs
                    .setFactoryPipeline(factoryPipelineRestDTOMapper.convert(factoryPipeline, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
