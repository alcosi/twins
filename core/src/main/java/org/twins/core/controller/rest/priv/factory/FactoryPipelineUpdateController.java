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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineListRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineUpdateRqDTOv1;
import org.twins.core.mappers.rest.factory.FactoryPipelineRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryPipelineUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryPipelineService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.FACTORY_PIPELINE_UPDATE)
public class FactoryPipelineUpdateController extends ApiController {
    private final FactoryPipelineRestDTOMapper factoryPipelineRestDTOMapper;
    private final FactoryPipelineUpdateDTOReverseMapper factoryPipelineUpdateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final FactoryPipelineService factoryPipelineService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryPipelineUpdateV1", summary = "Factory pipeline batch update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory pipeline batch updated", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryPipelineListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/factory_pipeline/v1")
    public ResponseEntity<?> factoryPipelineUpdateV1(
            @MapperContextBinding(roots = FactoryPipelineRestDTOMapper.class, response = FactoryPipelineListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody @Valid FactoryPipelineUpdateRqDTOv1 request) {
        FactoryPipelineListRsDTOv1 rs = new FactoryPipelineListRsDTOv1();
        try {
            List<TwinFactoryPipelineEntity> factoryPipelines = factoryPipelineService.updateFactoryPipelines(
                    factoryPipelineUpdateDTOReverseMapper.convertCollection(request.getFactoryPipelines()));
            rs
                    .setFactoryPipelineList(factoryPipelineRestDTOMapper.convertCollection(factoryPipelines, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
