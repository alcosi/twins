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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinflow.TransitionViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV2RestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowTransitionService;

import java.util.UUID;

@Tag(name = ApiTag.TRANSITION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TRANSITION_MANAGE, Permissions.TRANSITION_VIEW})
public class TransitionViewController extends ApiController {
    private final TwinflowTransitionService twinflowTransitionService;
    private final TransitionBaseV2RestDTOMapper transitionBaseV2RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "transitionViewV1", summary = "Returns transition details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transition details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TransitionViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/transition/{transitionId}/v1")
    public ResponseEntity<?> transitionViewV1(
            @MapperContextBinding(roots = TransitionBaseV2RestDTOMapper.class, response = TransitionViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ID) @PathVariable UUID transitionId) {
        TransitionViewRsDTOv1 rs = new TransitionViewRsDTOv1();
        try {
            rs
                    .setTransition(transitionBaseV2RestDTOMapper.convert(twinflowTransitionService.findEntitySafe(transitionId), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));

        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
