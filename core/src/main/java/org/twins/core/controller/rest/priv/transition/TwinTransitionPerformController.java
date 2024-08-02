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
import org.cambium.service.EntitySmartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.domain.transition.TransitionContext;
import org.twins.core.domain.transition.TransitionContextBatch;
import org.twins.core.domain.transition.TransitionResult;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.transition.TwinTransitionPerformBatchRqDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionPerformRqDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionPerformRsDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.link.TwinLinkCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinCreateRqRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.twinflow.TwinTransitionPerformRsRestDTOMapper;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinflow.TwinflowTransitionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Tag(description = "", name = ApiTag.TRANSITION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinTransitionPerformController extends ApiController {
    private final TwinService twinService;
    private final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    private final TwinflowTransitionService twinflowTransitionService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final AttachmentCUDRestDTOReverseMapperV2 attachmentCUDRestDTOReverseMapperV2;
    private final TwinLinkCUDRestDTOReverseMapperV2 twinLinkCUDRestDTOReverseMapperV2;
    private final TwinCreateRqRestDTOReverseMapper twinCreateRqRestDTOReverseMapper;
    private final TwinTransitionPerformRsRestDTOMapper twinTransitionPerformRsRestDTOMapper;

        @ParametersApiUserHeaders
        @Operation(operationId = "twinTransitionPerformV1", summary = "Perform twin transition by transition id. Transition will be performed only if current twin status is correct for given transition")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionPerformRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/transition/{transitionId}/perform/v1")
    public ResponseEntity<?> twinTransitionPerformV1(
            @MapperContextBinding(roots = TwinTransitionPerformRsRestDTOMapper.class, response = TwinTransitionPerformRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ID) @PathVariable UUID transitionId,
            @RequestBody TwinTransitionPerformRqDTOv1 request) {
        TwinTransitionPerformRsDTOv1 rs = new TwinTransitionPerformRsDTOv1();
        try {
            TwinEntity dbTwinEntity = twinService.findEntity(request.getTwinId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
            TransitionContext transitionContext = twinflowTransitionService.createTransitionContext(dbTwinEntity, transitionId);
            if (request.getContext() != null) {
                transitionContext
                        .setAttachmentCUD(attachmentCUDRestDTOReverseMapperV2.convert(request.getContext().getAttachments()))
                        .setTwinLinkCUD(twinLinkCUDRestDTOReverseMapperV2.convert(request.getContext().getTwinLinks()))
                        .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(request.getContext().getFields()))
                        .setNewTwinList(twinCreateRqRestDTOReverseMapper.convertCollection(request.getContext().getNewTwins()));
            }
            TransitionResult transitionResult = twinflowTransitionService.performTransition(transitionContext);
            twinTransitionPerformRsRestDTOMapper.map(transitionResult, rs, mapperContext);
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
    @Operation(operationId = "twinTransitionByAliasPerformV1", summary = "Perform twin transition by alias. An alias can be useful for performing transitions for twin from different statuses. " +
            "For incoming twin, the appropriate transition will be selected based on its current status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionPerformRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/transition_by_alias/{transitionAlias}/perform/v1")
    public ResponseEntity<?> twinTransitionByAliasPerformV1(
            @MapperContextBinding(roots = TwinTransitionPerformRsRestDTOMapper.class, response = TwinTransitionPerformRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ALIAS) @PathVariable String transitionAlias,
            @RequestBody TwinTransitionPerformRqDTOv1 request) {
        TwinTransitionPerformRsDTOv1 rs = new TwinTransitionPerformRsDTOv1();
        try {
            TwinEntity dbTwinEntity = twinService.findEntity(request.getTwinId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
            TransitionContext transitionContext = twinflowTransitionService.createTransitionContext(dbTwinEntity, transitionAlias);
            if (request.getContext() != null) {
                transitionContext
                        .setAttachmentCUD(attachmentCUDRestDTOReverseMapperV2.convert(request.getContext().getAttachments()))
                        .setTwinLinkCUD(twinLinkCUDRestDTOReverseMapperV2.convert(request.getContext().getTwinLinks()))
                        .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(request.getContext().getFields()))
                        .setNewTwinList(twinCreateRqRestDTOReverseMapper.convertCollection(request.getContext().getNewTwins()));
            }
            TransitionResult transitionResult = twinflowTransitionService.performTransition(transitionContext);
            twinTransitionPerformRsRestDTOMapper.map(transitionResult, rs, mapperContext);
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
    @Operation(operationId = "twinTransitionPerformBatchV1", summary = "Perform transition for batch of twins by transition id. Transition will be performed only if current twin status is correct for given transition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionPerformRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/transition/{transitionId}/perform/batch/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinTransitionPerformBatchV1(
            @MapperContextBinding(roots = TwinTransitionPerformRsRestDTOMapper.class, response = TwinTransitionPerformRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ID) @PathVariable UUID transitionId,
            @RequestBody TwinTransitionPerformBatchRqDTOv1 request) {
        TwinTransitionPerformRsDTOv1 rs = new TwinTransitionPerformRsDTOv1();
        try {
            List<TwinEntity> twinEntities = new ArrayList<>();
            for (UUID twinId : request.getTwinIdList()) {
                TwinEntity dbTwinEntity = twinService.findEntity(twinId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
                twinEntities.add(dbTwinEntity);
            }
            TransitionContext transitionContext = twinflowTransitionService.createTransitionContext(twinEntities, transitionId);
            if (request.getBatchContext() != null) {
                transitionContext
                        .setAttachmentCUD(attachmentCUDRestDTOReverseMapperV2.convert(request.getBatchContext().getAttachments()))
                        .setTwinLinkCUD(twinLinkCUDRestDTOReverseMapperV2.convert(request.getBatchContext().getTwinLinks()))
                        .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(request.getBatchContext().getFields()))
                        .setNewTwinList(twinCreateRqRestDTOReverseMapper.convertCollection(request.getBatchContext().getNewTwins()));
            }
            TransitionResult transitionResult = twinflowTransitionService.performTransition(transitionContext);
            twinTransitionPerformRsRestDTOMapper.map(transitionResult, rs, mapperContext);
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
    @Operation(operationId = "twinTransitionByAliasPerformBatchV1", summary = "Perform transition for batch of twins by alias. An alias can be useful for performing transitions for twins from different statuses. " +
            "For each incoming twin, the appropriate transition will be selected based on its current status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionPerformRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/transition_by_alias/{transitionAlias}/perform/batch/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinTransitionByAliasPerformBatchV1(
            @MapperContextBinding(roots = TwinTransitionPerformRsRestDTOMapper.class, response = TwinTransitionPerformRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ALIAS) @PathVariable String transitionAlias,
            @RequestBody TwinTransitionPerformBatchRqDTOv1 request) {
        TwinTransitionPerformRsDTOv1 rs = new TwinTransitionPerformRsDTOv1();
        try {
            List<TwinEntity> twinEntities = new ArrayList<>();
            for (UUID twinId : request.getTwinIdList()) {
                TwinEntity dbTwinEntity = twinService.findEntity(twinId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
                twinEntities.add(dbTwinEntity);
            }
            TransitionContextBatch transitionContextBatch = twinflowTransitionService.createTransitionContext(twinEntities, transitionAlias);
            for (TransitionContext transitionContext : transitionContextBatch.getAll()) {
                if (request.getBatchContext() != null)
                    transitionContext
                            .setAttachmentCUD(attachmentCUDRestDTOReverseMapperV2.convert(request.getBatchContext().getAttachments()))
                            .setTwinLinkCUD(twinLinkCUDRestDTOReverseMapperV2.convert(request.getBatchContext().getTwinLinks()))
                            .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(request.getBatchContext().getFields()))
                            .setNewTwinList(twinCreateRqRestDTOReverseMapper.convertCollection(request.getBatchContext().getNewTwins()));
            }
            TransitionResult transitionResult = twinflowTransitionService.performTransitions(transitionContextBatch);
            twinTransitionPerformRsRestDTOMapper.map(transitionResult, rs, mapperContext);
            rs
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    private TwinflowTransitionEntity selectTransition(TwinEntity twinEntity, List<TwinflowTransitionEntity> transitionEntityList) throws ServiceException {
        TwinflowTransitionEntity selectedTransition = null;
        for (TwinflowTransitionEntity transitionEntity : transitionEntityList) {
            if (twinEntity.getTwinStatusId().equals(transitionEntity.getSrcTwinStatusId())) {
                selectedTransition = transitionEntity;
                break;
            }
        }
        return selectedTransition;
    }
}
