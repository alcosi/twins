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
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.transition.TransitionContext;
import org.twins.core.domain.transition.TransitionContextBatch;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.transition.TwinTransitionDraftBatchRqDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionDraftRqDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionDraftRsDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.draft.DraftRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinCreateRqRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapperV2;
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
@ProtectedBy({Permissions.TRANSITION_MANAGE, Permissions.TRANSITION_DRAFT})
public class TwinTransitionDraftController extends ApiController {
    private final TwinService twinService;
    private final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    private final TwinflowTransitionService twinflowTransitionService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final AttachmentCUDRestDTOReverseMapperV2 attachmentCUDRestDTOReverseMapperV2;
    private final TwinLinkCUDRestDTOReverseMapperV2 twinLinkCUDRestDTOReverseMapperV2;
    private final TwinCreateRqRestDTOReverseMapper twinCreateRqRestDTOReverseMapper;
    private final DraftRestDTOMapper draftRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinTransitionDraftV1", summary = "Draft twin transition by transition id. Result will not be commited to db, and can be browsed by draftId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionDraftRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/transition/{transitionId}/draft/v1")
    public ResponseEntity<?> twinTransitionDraftV1(
            @MapperContextBinding(roots = DraftRestDTOMapper.class, response = TwinTransitionDraftRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ID) @PathVariable UUID transitionId,
            @RequestBody TwinTransitionDraftRqDTOv1 request) {
        TwinTransitionDraftRsDTOv1 rs = new TwinTransitionDraftRsDTOv1();
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
            DraftEntity transitionDraft = twinflowTransitionService.draftTransition(transitionContext);
            rs
                    .setTransitionDraft(draftRestDTOMapper.convert(transitionDraft, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinTransitionByAliasDraftV1", summary = "Draft twin transition by alias. Result will not be commited to db, and can be browsed by draftId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionDraftRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/transition_by_alias/{transitionAlias}/draft/v1")
    public ResponseEntity<?> twinTransitionByAliasDraftV1(
            @MapperContextBinding(roots = DraftRestDTOMapper.class, response = TwinTransitionDraftRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ALIAS) @PathVariable String transitionAlias,
            @RequestBody TwinTransitionDraftRqDTOv1 request) {
        TwinTransitionDraftRsDTOv1 rs = new TwinTransitionDraftRsDTOv1();
        try {
            TwinEntity dbTwinEntity = twinService.findEntitySafe(request.getTwinId());
            TransitionContext transitionContext = twinflowTransitionService.createTransitionContext(dbTwinEntity, transitionAlias);
            if (request.getContext() != null) {
                transitionContext
                        .setAttachmentCUD(attachmentCUDRestDTOReverseMapperV2.convert(request.getContext().getAttachments()))
                        .setTwinLinkCUD(twinLinkCUDRestDTOReverseMapperV2.convert(request.getContext().getTwinLinks()))
                        .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(request.getContext().getFields()))
                        .setNewTwinList(twinCreateRqRestDTOReverseMapper.convertCollection(request.getContext().getNewTwins()));
            }
            DraftEntity transitionDraft = twinflowTransitionService.draftTransition(transitionContext);
            rs
                    .setTransitionDraft(draftRestDTOMapper.convert(transitionDraft, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinTransitionDraftBatchV1", summary = "Draft transition for batch of twins by transition id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionDraftRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/transition/{transitionId}/draft/batch/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinTransitionDraftBatchV1(
            @MapperContextBinding(roots = DraftRestDTOMapper.class, response = TwinTransitionDraftRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ID) @PathVariable UUID transitionId,
            @RequestBody TwinTransitionDraftBatchRqDTOv1 request) {
        TwinTransitionDraftRsDTOv1 rs = new TwinTransitionDraftRsDTOv1();
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
            DraftEntity transitionDraft = twinflowTransitionService.draftTransition(transitionContext);
            rs
                    .setTransitionDraft(draftRestDTOMapper.convert(transitionDraft, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinTransitionByAliasDraftBatchV1", summary = "Draft transition for batch of twins by alias. An alias can be useful for Drafting transitions for twins from different statuses. " +
            "For each incoming twin, the appropriate transition will be selected based on its current status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionDraftRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/transition_by_alias/{transitionAlias}/draft/batch/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinTransitionByAliasDraftBatchV1(
            @MapperContextBinding(roots = DraftRestDTOMapper.class, response = TwinTransitionDraftRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ALIAS) @PathVariable String transitionAlias,
            @RequestBody TwinTransitionDraftBatchRqDTOv1 request) {
        TwinTransitionDraftRsDTOv1 rs = new TwinTransitionDraftRsDTOv1();
        try {
            List<TwinEntity> twinEntities = new ArrayList<>();
            for (UUID twinId : request.getTwinIdList()) {
                TwinEntity dbTwinEntity = twinService.findEntity(twinId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
                twinEntities.add(dbTwinEntity);
            }
            TransitionContextBatch transitionContexts = twinflowTransitionService.createTransitionContext(twinEntities, transitionAlias);
            for (TransitionContext transitionContext : transitionContexts.getAll()) {
                if (request.getBatchContext() != null)
                    transitionContext
                            .setAttachmentCUD(attachmentCUDRestDTOReverseMapperV2.convert(request.getBatchContext().getAttachments()))
                            .setTwinLinkCUD(twinLinkCUDRestDTOReverseMapperV2.convert(request.getBatchContext().getTwinLinks()))
                            .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(request.getBatchContext().getFields()))
                            .setNewTwinList(twinCreateRqRestDTOReverseMapper.convertCollection(request.getBatchContext().getNewTwins()));
            }
            DraftEntity draftEntity = twinflowTransitionService.draftTransitions(transitionContexts);
            rs
                    .setTransitionDraft(draftRestDTOMapper.convert(draftEntity, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
