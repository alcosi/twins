package org.twins.core.controller.rest.priv.transition;

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
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dto.rest.transition.TransitionTriggerUpdateRqDTOv1;
import org.twins.core.dto.rest.transition.TransitionTriggerViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TransitionTriggerRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TransitionTriggerUpdateDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowTransitionTriggerService;


@Tag(name = ApiTag.TRANSITION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TRANSITION_MANAGE, Permissions.TRANSITION_UPDATE})
public class TransitionTriggerUpdateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final TransitionTriggerUpdateDTOReverseMapper transitionTriggerUpdateDTOReverseMapper;
    private final TransitionTriggerRestDTOMapper transitionTriggerRestDTOMapper;
    private final TwinflowTransitionTriggerService twinflowTransitionTriggerService;

    @ParametersApiUserHeaders
    @Operation(operationId = "transitionTriggerUpdateV1", summary = "Update transition trigger")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transition trigger updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TransitionTriggerViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/transition_trigger/v1")
    public ResponseEntity<?> transitionTriggerUpdateV1(
            @MapperContextBinding(roots = TransitionTriggerRestDTOMapper.class, response = TransitionTriggerViewRsDTOv1.class) MapperContext mapperContext,
            @RequestBody TransitionTriggerUpdateRqDTOv1 request) {
        TransitionTriggerViewRsDTOv1 rs = new TransitionTriggerViewRsDTOv1();
        TwinflowTransitionTriggerEntity triggerEntity;
        try {
            triggerEntity = transitionTriggerUpdateDTOReverseMapper.convert(request.getTrigger());
            triggerEntity = twinflowTransitionTriggerService.updateTransitionTrigger(triggerEntity);
            rs
                    .setTrigger(transitionTriggerRestDTOMapper.convert(triggerEntity))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
