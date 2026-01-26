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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TwinflowFactoryRestDTOMapperV1;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowFactoryService;

import java.util.UUID;

@Tag(name = ApiTag.TWINFLOW)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWINFLOW_FACTORY_MANAGE, Permissions.TWINFLOW_FACTORY_VIEW})
public class TwinflowFactoryViewController extends ApiController {

    private final TwinflowFactoryService twinflowFactoryService;
    private final TwinflowFactoryRestDTOMapperV1 twinflowFactoryRestDTOMapperV1;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinflowFactoryViewV1", summary = "Returns twinflow factory view result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twinflow factory dto prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinflowFactoryViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twinflow/factory/{twinflowFactoryId}/v1")
    public ResponseEntity<?> twinflowFactoryViewV1(
            @MapperContextBinding(roots = TwinflowFactoryRestDTOMapperV1.class, response = TwinflowFactoryViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @PathVariable(name = "twinflowFactoryId") UUID twinflowFactoryId) {
        TwinflowFactoryViewRsDTOv1 rs = new TwinflowFactoryViewRsDTOv1();

        try {
            rs
                    .setTwinflowFactory(twinflowFactoryRestDTOMapperV1.convert(twinflowFactoryService.findEntitySafe(twinflowFactoryId), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
