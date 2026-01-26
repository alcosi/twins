package org.twins.core.controller.rest.priv.twinflow;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryUpdateRqDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryUpdateRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TwinflowFactoryRestDTOMapperV1;
import org.twins.core.mappers.rest.twinflow.TwinflowFactoryUpdateRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowFactoryService;

import java.util.List;

@Tag(name = ApiTag.TWINFLOW)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWINFLOW_FACTORY_MANAGE, Permissions.TWINFLOW_FACTORY_UPDATE})
public class TwinflowFactoryUpdateController extends ApiController {

    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final TwinflowFactoryRestDTOMapperV1 twinflowFactoryBaseRestDTOMapper;
    private final TwinflowFactoryUpdateRestDTOReverseMapper twinflowFactoryUpdateRestDTOReverseMapper;
    private final TwinflowFactoryService twinflowFactoryService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinflowFactoryUpdateV1", summary = "Update twinflow factory by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twinflow factory prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinflowFactoryUpdateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twinflow/factory/v1")
    public ResponseEntity<?> twinflowFactoryUpdateV1(
            @MapperContextBinding(roots = TwinflowFactoryRestDTOMapperV1.class, response = TwinflowFactoryUpdateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinflowFactoryUpdateRqDTOv1 request) {
        TwinflowFactoryUpdateRsDTOv1 rs = new TwinflowFactoryUpdateRsDTOv1();

        try {
            List<TwinflowFactoryEntity> twinflowFactoryEntities = twinflowFactoryUpdateRestDTOReverseMapper.convertCollection(request.getTwinflowFactories());
            twinflowFactoryEntities = twinflowFactoryService.updateTwinflowFactory(twinflowFactoryEntities);

            rs
                    .setTwinflowFactories(twinflowFactoryBaseRestDTOMapper.convertCollection(twinflowFactoryEntities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
