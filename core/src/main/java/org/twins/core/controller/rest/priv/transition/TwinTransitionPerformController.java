package org.twins.core.controller.rest.priv.transition;

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
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.transition.TransitionContext;
import org.twins.core.domain.transition.TransitionContextBatch;
import org.twins.core.domain.transition.TransitionResult;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.transition.TwinTransitionContextDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionPerformBatchRqDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionPerformRqDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionPerformRsDTOv2;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.link.TwinLinkCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinBasicFieldsRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinCreateRqRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.twinflow.TwinTransitionPerformRsRestDTOMapperV2;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinflow.TwinflowTransitionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Tag(description = "", name = ApiTag.TRANSITION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.TRANSITION_PERFORM)
public class TwinTransitionPerformController extends ApiController {
    private final TwinService twinService;
    private final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    private final TwinflowTransitionService twinflowTransitionService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final AttachmentCUDRestDTOReverseMapperV2 attachmentCUDRestDTOReverseMapperV2;
    private final TwinLinkCUDRestDTOReverseMapperV2 twinLinkCUDRestDTOReverseMapperV2;
    private final TwinCreateRqRestDTOReverseMapper twinCreateRqRestDTOReverseMapper;
    private final TwinTransitionPerformRsRestDTOMapperV2 twinTransitionPerformRsRestDTOMapperV2;
    private final TwinBasicFieldsRestDTOReverseMapper twinBasicFieldsRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinTransitionPerformV2", summary = "Perform twin transition by transition id. Transition will be performed only if current twin status is correct for given transition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionPerformRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/transition/{transitionId}/perform/v2")
    public ResponseEntity<?> twinTransitionPerformV2(
            @MapperContextBinding(roots = TwinTransitionPerformRsRestDTOMapperV2.class, response = TwinTransitionPerformRsDTOv2.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ID) @PathVariable UUID transitionId,
            @RequestBody TwinTransitionPerformRqDTOv1 request) {
        TwinTransitionPerformRsDTOv2 rs = new TwinTransitionPerformRsDTOv2();
        try {
            TwinEntity dbTwinEntity = twinService.findEntitySafe(request.getTwinId());
            TransitionContext transitionContext = twinflowTransitionService.createTransitionContext(dbTwinEntity, transitionId);
            mapTransitionContext(transitionContext, request.getContext());
            TransitionResult transitionResult = twinflowTransitionService.performTransition(transitionContext);
            twinTransitionPerformRsRestDTOMapperV2.map(transitionResult, rs, mapperContext);
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
    @Operation(operationId = "twinTransitionByAliasPerformV2", summary = "Perform twin transition by alias. An alias can be useful for performing transitions for twin from different statuses. " +
            "For incoming twin, the appropriate transition will be selected based on its current status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionPerformRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/transition_by_alias/{transitionAlias}/perform/v2")
    public ResponseEntity<?> twinTransitionByAliasPerformV2(
            @MapperContextBinding(roots = TwinTransitionPerformRsRestDTOMapperV2.class, response = TwinTransitionPerformRsDTOv2.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ALIAS) @PathVariable String transitionAlias,
            @RequestBody TwinTransitionPerformRqDTOv1 request) {
        TwinTransitionPerformRsDTOv2 rs = new TwinTransitionPerformRsDTOv2();
        try {
            TwinEntity dbTwinEntity = twinService.findEntitySafe(request.getTwinId());
            TransitionContext transitionContext = twinflowTransitionService.createTransitionContext(dbTwinEntity, transitionAlias);
            mapTransitionContext(transitionContext, request.getContext());
            TransitionResult transitionResult = twinflowTransitionService.performTransition(transitionContext);
            twinTransitionPerformRsRestDTOMapperV2.map(transitionResult, rs, mapperContext);
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
    @Operation(operationId = "twinTransitionPerformBatchV2", summary = "Perform transition for batch of twins by transition id. Transition will be performed only if current twin status is correct for given transition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionPerformRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/transition/{transitionId}/perform/batch/v2", method = RequestMethod.POST)
    public ResponseEntity<?> twinTransitionPerformBatchV2(
            @MapperContextBinding(roots = TwinTransitionPerformRsRestDTOMapperV2.class, response = TwinTransitionPerformRsDTOv2.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ID) @PathVariable UUID transitionId,
            @RequestBody TwinTransitionPerformBatchRqDTOv1 request) {
        TwinTransitionPerformRsDTOv2 rs = new TwinTransitionPerformRsDTOv2();
        try {
            List<TwinEntity> twinEntities = new ArrayList<>();
            for (UUID twinId : request.getTwinIdList()) {
                TwinEntity dbTwinEntity = twinService.findEntitySafe(twinId);
                twinEntities.add(dbTwinEntity);
            }
            TransitionContext transitionContext = twinflowTransitionService.createTransitionContext(twinEntities, transitionId);
            mapTransitionContext(transitionContext, request.getBatchContext());
            TransitionResult transitionResult = twinflowTransitionService.performTransition(transitionContext);
            twinTransitionPerformRsRestDTOMapperV2.map(transitionResult, rs, mapperContext);
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
    @Operation(operationId = "twinTransitionByAliasPerformBatchV2", summary = "Perform transition for batch of twins by alias. An alias can be useful for performing transitions for twins from different statuses. " +
            "For each incoming twin, the appropriate transition will be selected based on its current status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionPerformRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/transition_by_alias/{transitionAlias}/perform/batch/v2", method = RequestMethod.POST)
    public ResponseEntity<?> twinTransitionByAliasPerformBatchV2(
            @MapperContextBinding(roots = TwinTransitionPerformRsRestDTOMapperV2.class, response = TwinTransitionPerformRsDTOv2.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ALIAS) @PathVariable String transitionAlias,
            @RequestBody TwinTransitionPerformBatchRqDTOv1 request) {
        TwinTransitionPerformRsDTOv2 rs = new TwinTransitionPerformRsDTOv2();
        try {
            List<TwinEntity> twinEntities = new ArrayList<>();
            for (UUID twinId : request.getTwinIdList()) {
                TwinEntity dbTwinEntity = twinService.findEntitySafe(twinId);
                twinEntities.add(dbTwinEntity);
            }
            TransitionContextBatch transitionContextBatch = twinflowTransitionService.createTransitionContext(twinEntities, transitionAlias);
            for (TransitionContext transitionContext : transitionContextBatch.getAll()) {
                mapTransitionContext(transitionContext, request.getBatchContext());
            }
            TransitionResult transitionResult = twinflowTransitionService.performTransitions(transitionContextBatch);
            twinTransitionPerformRsRestDTOMapperV2.map(transitionResult, rs, mapperContext);
            rs
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    private void mapTransitionContext(TransitionContext transitionContext, TwinTransitionContextDTOv1 context) throws Exception {
        if (context != null)
            transitionContext
                    .setAttachmentCUD(attachmentCUDRestDTOReverseMapperV2.convert(context.getAttachments()))
                    .setTwinLinkCUD(twinLinkCUDRestDTOReverseMapperV2.convert(context.getTwinLinks()))
                    .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(context.getFields()))
                    .setNewTwinList(twinCreateRqRestDTOReverseMapper.convertCollection(context.getNewTwins()))
                    .setBasics(twinBasicFieldsRestDTOReverseMapper.convert(context.getBasics()));
    }
}
