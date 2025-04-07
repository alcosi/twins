package org.twins.core.controller.rest.priv.motion;

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
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.transition.TransitionContext;
import org.twins.core.domain.transition.TransitionResult;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.motion.MotionPerformRqDTOv1;
import org.twins.core.dto.rest.motion.MotionPerformRsDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionContextDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionPerformBatchRqDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionPerformRsDTOv2;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.link.TwinLinkCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinBasicFieldsRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinCreateRqRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.twinflow.TwinTransitionPerformRsRestDTOMapperV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldMotionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Tag(description = "", name = ApiTag.MOTION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class MotionPerformController extends ApiController {
    private final TwinService twinService;
    private final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    private final TwinClassFieldMotionService motionService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final AttachmentCUDRestDTOReverseMapperV2 attachmentCUDRestDTOReverseMapperV2;
    private final TwinLinkCUDRestDTOReverseMapperV2 twinLinkCUDRestDTOReverseMapperV2;
    private final TwinCreateRqRestDTOReverseMapper twinCreateRqRestDTOReverseMapper;
    private final TwinTransitionPerformRsRestDTOMapperV2 motionPerformRsRestDTOMapperV2;
    private final TwinBasicFieldsRestDTOReverseMapper twinBasicFieldsRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "motionPerformV2", summary = "Perform twin field motion by motion id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionPerformRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/motion/{motionId}/perform/v2")
    public ResponseEntity<?> motionPerformV2(
            @MapperContextBinding(roots = TwinTransitionPerformRsRestDTOMapperV2.class, response = TwinTransitionPerformRsDTOv2.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ID) @PathVariable UUID motionId,
            @RequestBody MotionPerformRqDTOv1 request) {
        MotionPerformRsDTOv1 rs = new MotionPerformRsDTOv1();
        try {
            TwinEntity dbTwinEntity = twinService.findEntitySafe(request.getTwinId());
            TransitionContext motionContext = motionService.createTransitionContext(dbTwinEntity, motionId);
            mapTransitionContext(motionContext, request.getContext());
            TransitionResult motionResult = motionService.runMotion(motionContext);
            motionPerformRsRestDTOMapperV2.map(motionResult, rs, mapperContext);
            rs
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "motionPerformBatchV2", summary = "Perform motion for batch of twins by motion id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionPerformRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/motion/{motionId}/perform/batch/v2", method = RequestMethod.POST)
    public ResponseEntity<?> motionPerformBatchV2(
            @MapperContextBinding(roots = TwinTransitionPerformRsRestDTOMapperV2.class, response = TwinTransitionPerformRsDTOv2.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ID) @PathVariable UUID motionId,
            @RequestBody TwinTransitionPerformBatchRqDTOv1 request) {
        MotionPerformRsDTOv1 rs = new MotionPerformRsDTOv1();
        try {
            List<TwinEntity> twinEntities = new ArrayList<>();
            for (UUID twinId : request.getTwinIdList()) {
                TwinEntity dbTwinEntity = twinService.findEntitySafe(twinId);
                twinEntities.add(dbTwinEntity);
            }
            TransitionContext motionContext = motionService.createTransitionContext(twinEntities, motionId);
            mapTransitionContext(motionContext, request.getBatchContext());
            TransitionResult motionResult = motionService.performTransition(motionContext);
            motionPerformRsRestDTOMapperV2.map(motionResult, rs, mapperContext);
            rs
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    private void mapTransitionContext(TransitionContext motionContext, TwinTransitionContextDTOv1 context) throws Exception {
        if (context != null)
            motionContext
                    .setAttachmentCUD(attachmentCUDRestDTOReverseMapperV2.convert(context.getAttachments()))
                    .setTwinLinkCUD(twinLinkCUDRestDTOReverseMapperV2.convert(context.getTwinLinks()))
                    .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(context.getFields()))
                    .setNewTwinList(twinCreateRqRestDTOReverseMapper.convertCollection(context.getNewTwins()))
                    .setBasics(twinBasicFieldsRestDTOReverseMapper.convert(context.getBasics()));
    }
}
