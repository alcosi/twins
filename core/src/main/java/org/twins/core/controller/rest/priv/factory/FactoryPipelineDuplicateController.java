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
import org.twins.core.dto.rest.factory.FactoryPipelineDuplicateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineListRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryPipelineDuplicateRestDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryPipelineRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryPipelineDuplicateService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_PIPELINE_CREATE})
public class FactoryPipelineDuplicateController extends ApiController {
    private final FactoryPipelineDuplicateService factoryPipelineDuplicateService;
    private final FactoryPipelineRestDTOMapper factoryPipelineRestDTOMapper;
    private final FactoryPipelineDuplicateRestDTOReverseMapper factoryPipelineDuplicateRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryPipelineDuplicateV1", summary = "Duplicates factory pipelines")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory pipelines copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryPipelineListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_pipeline/duplicate/v1")
    public ResponseEntity<?> factoryPipelineDuplicateV1(
            @MapperContextBinding(roots = FactoryPipelineRestDTOMapper.class, response = FactoryPipelineListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Valid @RequestBody FactoryPipelineDuplicateRqDTOv1 request) {
        var rs = new FactoryPipelineListRsDTOv1();

        try {
            var duplicates = factoryPipelineDuplicateRestDTOReverseMapper.convertCollection(request.duplicates, mapperContext);
            var duplicatedPipelines = factoryPipelineDuplicateService.duplicate(duplicates);
            rs
                    .setFactoryPipelineList(factoryPipelineRestDTOMapper.convertCollection(duplicatedPipelines, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
