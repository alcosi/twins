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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TransitionCreateRqDTOv1;
import org.twins.core.dto.rest.twinflow.TransitionListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV2RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TransitionCreateRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowTransitionService;

import java.util.List;

@Tag(description = "", name = ApiTag.TWINFLOW)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TRANSITION_MANAGE, Permissions.TRANSITION_CREATE})
public class TransitionCreateController extends ApiController {
    private final TransitionCreateRestDTOReverseMapper transitionCreateRestDTOReverseMapper;
    private final TwinflowTransitionService twinflowTransitionService;
    private final TransitionBaseV2RestDTOMapper transitionBaseV2RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "transitionCreateV1", summary = "Create transitions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transition data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TransitionListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/transition/v1")
    public ResponseEntity<?> transitionCreateV1(
            @MapperContextBinding(roots = TransitionBaseV2RestDTOMapper.class, response = TransitionListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TransitionCreateRqDTOv1 request) {
        TransitionListRsDTOv1 rs = new TransitionListRsDTOv1();
        try {
            List<TwinflowTransitionEntity> transitionEntities = transitionCreateRestDTOReverseMapper.convertCollection(request.getTransitions());
            List<TwinflowTransitionEntity> resultEntities = twinflowTransitionService.createTwinflowTransitions(transitionEntities);
            rs
                    .setTransitions(transitionBaseV2RestDTOMapper.convertCollection(resultEntities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
