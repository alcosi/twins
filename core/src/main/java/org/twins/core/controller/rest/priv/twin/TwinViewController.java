package org.twins.core.controller.rest.priv.twin;

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
import org.twins.core.controller.rest.annotation.MapperModeParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinRsDTOv1;
import org.twins.core.dto.rest.twin.TwinRsDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.*;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinViewController extends ApiController {
    final AuthService authService;
    final TwinService twinService;
    final TwinRestDTOMapper twinRestDTOMapper;
    final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @Deprecated
    @ParametersApiUserHeaders
    @Operation(operationId = "twinViewV1", summary = "Returns twin data by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin/{twinId}/v1")

    public ResponseEntity<?> twinViewV1(
            @BindParam(dto = TwinRsDTOv1.class,
            block = {

            }) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
    ) {

        TwinRsDTOv1 rs = new TwinRsDTOv1();
        try {
            rs
                    .twin(twinRestDTOMapper.convert(twinService.findEntitySafe(twinId), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinViewV2", summary = "Returns twin data by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin/{twinId}/v2")
    public ResponseEntity<?> twinViewV2(
            MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @MapperModeParam MapperMode.TwinByLinkMode showRelatedByLinkTwinMode,
            @MapperModeParam MapperMode.TwinByHeadMode showRelatedByHeadTwinMode,
            @MapperModeParam(def = MapperMode.TwinUserMode.Fields.SHORT) MapperMode.TwinUserMode showTwinUserMode,
            @MapperModeParam MapperMode.TwinStatusMode showTwinStatusMode,
            @MapperModeParam MapperMode.TransitionStatusMode showTransitionStatusMode,
            @MapperModeParam MapperMode.TwinClassStatusMode showClassStatusMode,
            @MapperModeParam(def = MapperMode.TwinClassMode.Fields.SHORT) MapperMode.TwinClassMode showClassMode,
            @MapperModeParam(def = MapperMode.TwinClassFieldMode.Fields.SHORT) MapperMode.TwinClassFieldMode showClassFieldMode,
            @MapperModeParam MapperMode.TwinClassTagMode showClassTagMode,
            @MapperModeParam MapperMode.TwinClassMarkerMode showClassMarkerMode,
            @MapperModeParam MapperMode.TwinMode showTwinMode,
            @RequestParam(name = RestRequestParam.showTwinFieldMode, defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @MapperModeParam(def = MapperMode.TwinAttachmentCollectionMode.Fields.ALL) MapperMode.TwinAttachmentCollectionMode showTwinAttachmentCollectionMode,
            @MapperModeParam MapperMode.TwinAttachmentMode showTwinAttachmentMode,
            @MapperModeParam MapperMode.AttachmentUserMode showAttachmentUserMode,
            @MapperModeParam MapperMode.TwinMarkerMode showTwinMarkerMode,
            @MapperModeParam MapperMode.TwinTagMode showTwinTagMode,
            @MapperModeParam MapperMode.TwinAliasMode showTwinAliasMode,
            @MapperModeParam MapperMode.TwinLinkMode showTwinLinkMode,
            @MapperModeParam MapperMode.TwinLinkOnLinkMode showTwinLinkOnLinkMode,
            @MapperModeParam MapperMode.TwinLinkUserMode showTwinLinkUserMode,
            @MapperModeParam MapperMode.TwinTransitionMode showTwinTransitionMode,
            @MapperModeParam MapperMode.TwinActionMode showTwinActionMode
    ) {
        TwinRsDTOv2 rs = new TwinRsDTOv2();
        try {
            rs
                    .twin(twinRestDTOMapperV2.convert(twinService.findEntitySafe(twinId), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinViewByAliasV1", summary = "Returns twin data by alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin_by_alias/{twinAlias}/v1")
    public ResponseEntity<?> twinViewByAliasV1(
            MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable String twinAlias,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @MapperModeParam MapperMode.TwinByLinkMode showRelatedByLinkTwinMode,
            @MapperModeParam MapperMode.TwinByHeadMode showRelatedByHeadTwinMode,
            @MapperModeParam(def = MapperMode.TwinUserMode.Fields.SHORT) MapperMode.TwinUserMode showTwinUserMode,
            @MapperModeParam MapperMode.TwinStatusMode showTwinStatusMode,
            @MapperModeParam MapperMode.TransitionStatusMode showTransitionStatusMode,
            @MapperModeParam MapperMode.TwinClassStatusMode showClassStatusMode,
            @MapperModeParam(def = MapperMode.TwinClassMode.Fields.SHORT) MapperMode.TwinClassMode showClassMode,
            @MapperModeParam(def = MapperMode.TwinClassFieldMode.Fields.SHORT) MapperMode.TwinClassFieldMode showClassFieldMode,
            @MapperModeParam MapperMode.TwinClassTagMode showClassTagMode,
            @MapperModeParam MapperMode.TwinClassMarkerMode showClassMarkerMode,
            @MapperModeParam MapperMode.TwinMode showTwinMode,
            @RequestParam(name = RestRequestParam.showTwinFieldMode, defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @MapperModeParam(def = MapperMode.TwinAttachmentCollectionMode.Fields.ALL) MapperMode.TwinAttachmentCollectionMode showTwinAttachmentCollectionMode,
            @MapperModeParam MapperMode.TwinAttachmentMode showTwinAttachmentMode,
            @MapperModeParam MapperMode.AttachmentUserMode showAttachmentUserMode,
            @MapperModeParam MapperMode.TwinMarkerMode showTwinMarkerMode,
            @MapperModeParam MapperMode.TwinTagMode showTwinTagMode,
            @MapperModeParam MapperMode.TwinAliasMode showTwinAliasMode,
            @MapperModeParam MapperMode.TwinLinkMode showTwinLinkMode,
            @MapperModeParam MapperMode.TwinLinkOnLinkMode showTwinLinkOnLinkMode,
            @MapperModeParam MapperMode.TwinLinkUserMode showTwinLinkUserMode,
            @MapperModeParam MapperMode.TwinTransitionMode showTwinTransitionMode,
            @MapperModeParam MapperMode.TwinActionMode showTwinActionMode
    ) {
        TwinRsDTOv1 rs = new TwinRsDTOv1();
        try {
            rs
                    .twin(twinRestDTOMapper.convert(twinService.findTwinByAlias(twinAlias), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinViewByAliasV2", summary = "Returns twin data by alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin_by_alias/{twinAlias}/v2")
    public ResponseEntity<?> twinViewByAliasV2(
            MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable String twinAlias,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @MapperModeParam MapperMode.TwinByLinkMode showRelatedByLinkTwinMode,
            @MapperModeParam MapperMode.TwinByHeadMode showRelatedByHeadTwinMode,
            @MapperModeParam(def = MapperMode.TwinUserMode.Fields.SHORT) MapperMode.TwinUserMode showTwinUserMode,
            @MapperModeParam MapperMode.TwinStatusMode showTwinStatusMode,
            @MapperModeParam MapperMode.TransitionStatusMode showTransitionStatusMode,
            @MapperModeParam MapperMode.TwinClassStatusMode showClassStatusMode,
            @MapperModeParam(def = MapperMode.TwinClassMode.Fields.SHORT) MapperMode.TwinClassMode showClassMode,
            @MapperModeParam(def = MapperMode.TwinClassFieldMode.Fields.SHORT) MapperMode.TwinClassFieldMode showClassFieldMode,
            @MapperModeParam MapperMode.TwinClassTagMode showClassTagMode,
            @MapperModeParam MapperMode.TwinClassMarkerMode showClassMarkerMode,
            @MapperModeParam MapperMode.TwinMode showTwinMode,
            @RequestParam(name = RestRequestParam.showTwinFieldMode, defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @MapperModeParam(def = MapperMode.TwinAttachmentCollectionMode.Fields.ALL) MapperMode.TwinAttachmentCollectionMode showTwinAttachmentCollectionMode,
            @MapperModeParam MapperMode.TwinAttachmentMode showTwinAttachmentMode,
            @MapperModeParam MapperMode.AttachmentUserMode showAttachmentUserMode,
            @MapperModeParam MapperMode.TwinMarkerMode showTwinMarkerMode,
            @MapperModeParam MapperMode.TwinTagMode showTwinTagMode,
            @MapperModeParam MapperMode.TwinAliasMode showTwinAliasMode,
            @MapperModeParam MapperMode.TwinLinkMode showTwinLinkMode,
            @MapperModeParam MapperMode.TwinLinkOnLinkMode showTwinLinkOnLinkMode,
            @MapperModeParam MapperMode.TwinLinkUserMode showTwinLinkUserMode,
            @MapperModeParam MapperMode.TwinTransitionMode showTwinTransitionMode,
            @MapperModeParam MapperMode.TwinActionMode showTwinActionMode
    ) {
        TwinRsDTOv1 rs = new TwinRsDTOv1();
        try {
            rs
                    .twin(twinRestDTOMapper.convert(twinService.findTwinByAlias(twinAlias), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
