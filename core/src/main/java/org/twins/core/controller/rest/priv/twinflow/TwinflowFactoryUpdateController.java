package org.twins.core.controller.rest.priv.twinflow;

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
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinflow.*;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TwinflowFactoryBaseRestDTOMapperV1;
import org.twins.core.mappers.rest.twinflow.TwinflowFactoryUpdateRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowFactoryService;

import java.util.UUID;

@Tag(name = ApiTag.TWINFLOW)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWINFLOW_FACTORY_MANAGE, Permissions.TWINFLOW_FACTORY_UPDATE})
public class TwinflowFactoryUpdateController extends ApiController {

    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final TwinflowFactoryBaseRestDTOMapperV1 twinflowFactoryBaseRestDTOMapper;
    private final TwinflowFactoryUpdateRestDTOReverseMapper twinflowFactoryUpdateRestDTOReverseMapper;
    private final TwinflowFactoryService twinflowFactoryService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinflowFactoryUpdateV1", summary = "Update twinflow factory by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twinflow factory prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinflowFactoryUpdateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twinflow/factory/{twinflowFactoryId}/v1")
    public ResponseEntity<?> twinflowFactoryUpdateV1(
            @MapperContextBinding(roots = TwinflowFactoryBaseRestDTOMapperV1.class, response = TwinflowFactoryUpdateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_FACTORY_ID) @PathVariable UUID twinflowFactoryId,
            @RequestBody TwinflowFactoryUpdateRqDTOv1 request) {
        TwinflowFactoryUpdateRsDTOv1 rs = new TwinflowFactoryUpdateRsDTOv1();
        try {
            TwinflowFactoryEntity twinflowFactoryEntity = twinflowFactoryUpdateRestDTOReverseMapper.convert(request).setId(twinflowFactoryId);
            twinflowFactoryEntity = twinflowFactoryService.updateTwinflowFactory(twinflowFactoryEntity);

            rs
                    .setTwinflowFactory(twinflowFactoryBaseRestDTOMapper.convert(twinflowFactoryEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
