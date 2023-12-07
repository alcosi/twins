package org.twins.core.controller.rest.priv.twinflow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.domain.transition.TransitionContext;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinflow.TwinTransitionPerformBatchRqDTOv1;
import org.twins.core.dto.rest.twinflow.TwinTransitionPerformRqDTOv1;
import org.twins.core.dto.rest.twinflow.TwinTransitionPerformRsDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.link.TwinLinkRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.*;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinTransitionPerformRsRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinTransitionRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinflow.TwinflowTransitionService;
import org.twins.core.service.user.UserService;

import java.util.List;
import java.util.UUID;

@Tag(description = "", name = ApiTag.TRANSITION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinTransitionPerformController extends ApiController {
    final AuthService authService;
    final TwinService twinService;
    final UserService userService;
    final TwinFieldValueRestDTOReverseMapper twinFieldValueRestDTOReverseMapper;
    final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    final TwinflowTransitionService twinflowTransitionService;
    final TwinUpdateRestDTOReverseMapper twinUpdateRestDTOReverseMapper;
    final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    final AttachmentCUDRestDTOReverseMapperV2 attachmentCUDRestDTOReverseMapperV2;
    final TwinLinkCUDRestDTOReverseMapperV2 twinLinkCUDRestDTOReverseMapperV2;
    final TwinTransitionPerformRsRestDTOMapper twinTransitionPerformRsRestDTOMapper;
    final TwinCreateRqRestDTOReverseMapper twinCreateRqRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinTransitionPerformV1", summary = "Perform twin transition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionPerformRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/transition/{transitionId}/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinTransitionPerformV1(
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ID) @PathVariable UUID transitionId,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._SHORT) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._SHORT) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestParam(name = RestRequestParam.showClassMode, defaultValue = TwinClassBaseRestDTOMapper.ClassMode._SHORT) TwinClassBaseRestDTOMapper.ClassMode showClassMode,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @RequestParam(name = RestRequestParam.showTwinMode, defaultValue = TwinBaseRestDTOMapper.TwinMode._DETAILED) TwinBaseRestDTOMapper.TwinMode showTwinMode,
            @RequestParam(name = RestRequestParam.showTwinFieldMode, defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @RequestParam(name = RestRequestParam.showAttachmentMode, defaultValue = AttachmentViewRestDTOMapper.Mode._HIDE) AttachmentViewRestDTOMapper.Mode showAttachmentMode,
            @RequestParam(name = RestRequestParam.showTwinLinkMode, defaultValue = TwinLinkRestDTOMapper.Mode._HIDE) TwinLinkRestDTOMapper.Mode showTwinLinkMode,
            @RequestParam(name = RestRequestParam.showLinkMode, defaultValue = LinkRestDTOMapper.Mode._HIDE) LinkRestDTOMapper.Mode showLinkMode,
            @RequestParam(name = RestRequestParam.showTwinTransitionMode, defaultValue = TwinTransitionRestDTOMapper.Mode._HIDE) TwinTransitionRestDTOMapper.Mode showTwinTransitionMode,
            @RequestBody TwinTransitionPerformRqDTOv1 request) {
        TwinTransitionPerformRsDTOv1 rs = new TwinTransitionPerformRsDTOv1();
        try {
            TwinflowTransitionEntity transitionEntity = twinflowTransitionService.findEntitySafe(transitionId);
            TwinEntity dbTwinEntity = twinService.findEntity(request.getTwinId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
            if (!dbTwinEntity.getTwinStatusId().equals(transitionEntity.getSrcTwinStatusId()))
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, transitionEntity.easyLog(EasyLoggable.Level.NORMAL) + " can not be performed for " + dbTwinEntity.easyLog(EasyLoggable.Level.NORMAL));
            TransitionContext transitionContext = new TransitionContext();
            transitionContext
                    .setTransitionEntity(transitionEntity)
                    .addTargetTwin(dbTwinEntity);
            if (request.getContext() != null) {
                transitionContext
                        .setAttachmentCUD(attachmentCUDRestDTOReverseMapperV2.convert(request.getContext().getAttachments()))
                        .setTwinLinkCUD(twinLinkCUDRestDTOReverseMapperV2.convert(request.getContext().getTwinLinks()))
                        .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(request.getContext().getFields()))
                        .setNewTwinList(twinCreateRqRestDTOReverseMapper.convertList(request.getContext().getNewTwins()));
            }
            TwinflowTransitionService.TransitionResult transitionResult = twinflowTransitionService.performTransition(transitionContext);
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(lazyRelation)
                    .setMode(showUserMode)
                    .setMode(showStatusMode)
                    .setMode(showClassMode)
                    .setMode(showClassFieldMode)
                    .setMode(showTwinMode)
                    .setMode(showTwinFieldMode)
                    .setMode(showAttachmentMode)
                    .setMode(showTwinLinkMode)
                    .setMode(showLinkMode)
                    .setMode(showTwinTransitionMode);
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
    @Operation(operationId = "twinTransitionPerformBatchV1", summary = "Perform transition for batch of twins")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTransitionPerformRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/transition/{transitionId}/batch/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinTransitionPerformBatchV1(
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ID) @PathVariable UUID transitionId,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._SHORT) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._SHORT) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestParam(name = RestRequestParam.showClassMode, defaultValue = TwinClassBaseRestDTOMapper.ClassMode._SHORT) TwinClassBaseRestDTOMapper.ClassMode showClassMode,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @RequestParam(name = RestRequestParam.showTwinMode, defaultValue = TwinBaseRestDTOMapper.TwinMode._DETAILED) TwinBaseRestDTOMapper.TwinMode showTwinMode,
            @RequestParam(name = RestRequestParam.showTwinFieldMode, defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @RequestParam(name = RestRequestParam.showAttachmentMode, defaultValue = AttachmentViewRestDTOMapper.Mode._HIDE) AttachmentViewRestDTOMapper.Mode showAttachmentMode,
            @RequestParam(name = RestRequestParam.showTwinLinkMode, defaultValue = TwinLinkRestDTOMapper.Mode._HIDE) TwinLinkRestDTOMapper.Mode showTwinLinkMode,
            @RequestParam(name = RestRequestParam.showLinkMode, defaultValue = LinkRestDTOMapper.Mode._HIDE) LinkRestDTOMapper.Mode showLinkMode,
            @RequestParam(name = RestRequestParam.showTwinTransitionMode, defaultValue = TwinTransitionRestDTOMapper.Mode._HIDE) TwinTransitionRestDTOMapper.Mode showTwinTransitionMode,
            @RequestBody TwinTransitionPerformBatchRqDTOv1 request) {
        TwinTransitionPerformRsDTOv1 rs = new TwinTransitionPerformRsDTOv1();
        try {
            TwinflowTransitionEntity transitionEntity = twinflowTransitionService.findEntitySafe(transitionId);
            TransitionContext transitionContext = new TransitionContext();
            transitionContext.setTransitionEntity(transitionEntity);
            for (UUID twinId : request.getTwinIdList()) {
                TwinEntity dbTwinEntity = twinService.findEntity(twinId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
                if (!dbTwinEntity.getTwinStatusId().equals(transitionEntity.getSrcTwinStatusId()))
                    throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, transitionEntity.easyLog(EasyLoggable.Level.NORMAL) + " can not be performed for " + dbTwinEntity.easyLog(EasyLoggable.Level.NORMAL));
                transitionContext.addTargetTwin(dbTwinEntity);
            }
            if (request.getBatchContext() != null) {
                transitionContext
                        .setAttachmentCUD(attachmentCUDRestDTOReverseMapperV2.convert(request.getBatchContext().getAttachments()))
                        .setTwinLinkCUD(twinLinkCUDRestDTOReverseMapperV2.convert(request.getBatchContext().getTwinLinks()))
                        .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(request.getBatchContext().getFields()))
                        .setNewTwinList(twinCreateRqRestDTOReverseMapper.convertList(request.getBatchContext().getNewTwins()));
            }
            TwinflowTransitionService.TransitionResult transitionResult = twinflowTransitionService.performTransition(transitionContext);
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(lazyRelation)
                    .setMode(showUserMode)
                    .setMode(showStatusMode)
                    .setMode(showClassMode)
                    .setMode(showClassFieldMode)
                    .setMode(showTwinMode)
                    .setMode(showTwinFieldMode)
                    .setMode(showAttachmentMode)
                    .setMode(showTwinLinkMode)
                    .setMode(showLinkMode)
                    .setMode(showTwinTransitionMode);
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
}
