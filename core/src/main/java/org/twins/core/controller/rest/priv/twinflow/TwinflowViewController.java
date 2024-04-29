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
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinflow.TwinflowViewRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowBaseV3RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowTransitionBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twinflow.TwinflowService;

import java.util.UUID;

@Tag(name = ApiTag.TWINFLOW)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinflowViewController extends ApiController {
    final TwinflowService twinflowService;
    final TwinflowBaseV3RestDTOMapper twinflowBaseV3RestDTOMapper;
    final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinflowViewV1", summary = "Returns twinflow details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twinflow details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinflowViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twinflow/{twinflowId}/v1")
    public ResponseEntity<?> twinflowViewV1(
            @Parameter(example = DTOExamples.TWINFLOW_ID) @PathVariable UUID twinflowId,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @RequestParam(name = RestRequestParam.showTwinflowMode, defaultValue = TwinflowBaseV1RestDTOMapper.TwinflowMode._SHORT) TwinflowBaseV1RestDTOMapper.TwinflowMode showTwinflowMode,
            @RequestParam(name = RestRequestParam.showTwinflowTransitionMode, defaultValue = TwinflowTransitionBaseV1RestDTOMapper.TwinflowTransitionMode._SHORT) TwinflowTransitionBaseV1RestDTOMapper.TwinflowTransitionMode showTwinflowTransitionMode,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._SHORT) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._SHORT) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showPermissionMode, defaultValue = PermissionRestDTOMapper.Mode._HIDE) PermissionRestDTOMapper.Mode showPermissionMode) {
        TwinflowViewRsDTOv1 rs = new TwinflowViewRsDTOv1();
        try {
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(lazyRelation)
                    .setMode(showTwinflowMode)
                    .setMode(showTwinflowTransitionMode)
                    .setMode(showStatusMode)
                    .setMode(showUserMode)
                    .setMode(showPermissionMode);
            rs
                    .setTwinflow(twinflowBaseV3RestDTOMapper.convert(twinflowService.findEntitySafe(twinflowId), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));

        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
