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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinRsDTOv1;
import org.twins.core.dto.rest.twin.TwinRsDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.*;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinTransitionRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
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

    @ParametersApiUserHeaders
    @Operation(operationId = "twinViewV1", summary = "Returns twin data by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/{twinId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinViewV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @RequestParam(name = RestRequestParam.showRelatedByLinkTwinMode, defaultValue = RelatedByLinkTwinMode._WHITE) RelatedByLinkTwinMode showRelatedByLinkTwinMode,
            @RequestParam(name = RestRequestParam.showRelatedByHeadTwinMode, defaultValue = RelatedByHeadTwinMode._WHITE) RelatedByHeadTwinMode showRelatedByHeadTwinMode,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._SHORT) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._SHORT) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestParam(name = RestRequestParam.showClassMode, defaultValue = TwinClassBaseRestDTOMapper.ClassMode._SHORT) TwinClassBaseRestDTOMapper.ClassMode showClassMode,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @RequestParam(name = RestRequestParam.showClassStatusMode, defaultValue = TwinClassRestDTOMapper.StatusMode._HIDE) TwinClassRestDTOMapper.StatusMode showClassStatusMode,
            @RequestParam(name = RestRequestParam.showClassMarkerMode, defaultValue = TwinClassRestDTOMapper.MarkerMode._HIDE) TwinClassRestDTOMapper.MarkerMode showClassMarkerMode,
            @RequestParam(name = RestRequestParam.showClassTagMode, defaultValue = TwinClassRestDTOMapper.TagMode._HIDE) TwinClassRestDTOMapper.TagMode showClassTagMode,
            @RequestParam(name = RestRequestParam.showTwinMode, defaultValue = TwinBaseRestDTOMapper.TwinMode._DETAILED) TwinBaseRestDTOMapper.TwinMode showTwinMode,
            @RequestParam(name = RestRequestParam.showTwinFieldMode, defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @RequestParam(name = RestRequestParam.showTwinAttachmentMode, defaultValue = AttachmentViewRestDTOMapper.TwinAttachmentMode._ALL) AttachmentViewRestDTOMapper.TwinAttachmentMode showTwinAttachmentMode,
            @RequestParam(name = RestRequestParam.showAttachmentMode, defaultValue = AttachmentViewRestDTOMapper.Mode._HIDE) AttachmentViewRestDTOMapper.Mode showAttachmentMode,
            @RequestParam(name = RestRequestParam.showTwinMarkerMode, defaultValue = TwinBaseV3RestDTOMapper.TwinMarkerMode._HIDE) TwinBaseV3RestDTOMapper.TwinMarkerMode showTwinMarkerMode,
            @RequestParam(name = RestRequestParam.showTwinTagMode, defaultValue = TwinBaseV3RestDTOMapper.TwinTagMode._HIDE) TwinBaseV3RestDTOMapper.TwinTagMode showTwinTagMode,
            @RequestParam(name = RestRequestParam.showTwinLinkMode, defaultValue = TwinLinkRestDTOMapper.Mode._HIDE) TwinLinkRestDTOMapper.Mode showTwinLinkMode,
            @RequestParam(name = RestRequestParam.showLinkMode, defaultValue = LinkRestDTOMapper.Mode._HIDE) LinkRestDTOMapper.Mode showLinkMode,
            @RequestParam(name = RestRequestParam.showTwinTransitionMode, defaultValue = TwinTransitionRestDTOMapper.Mode._HIDE) TwinTransitionRestDTOMapper.Mode showTwinTransitionMode) {
        TwinRsDTOv1 rs = new TwinRsDTOv1();
        try {
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(lazyRelation)
                    .setMode(showRelatedByHeadTwinMode)
                    .setMode(showRelatedByLinkTwinMode)
                    .setMode(showUserMode)
                    .setMode(showStatusMode)
                    .setMode(showClassMode)
                    .setMode(showClassFieldMode)
                    .setMode(showClassStatusMode)
                    .setMode(showClassMarkerMode)
                    .setMode(showClassTagMode)
                    .setMode(showTwinMode)
                    .setMode(showTwinFieldMode)
                    .setMode(showTwinAttachmentMode)
                    .setMode(showAttachmentMode)
                    .setMode(showTwinMarkerMode)
                    .setMode(showTwinTagMode)
                    .setMode(showTwinLinkMode)
                    .setMode(showLinkMode)
                    .setMode(showTwinTransitionMode);
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
    @RequestMapping(value = "/private/twin/{twinId}/v2", method = RequestMethod.GET)
    public ResponseEntity<?> twinViewV2(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @RequestParam(name = RestRequestParam.showRelatedByLinkTwinMode, defaultValue = RelatedByLinkTwinMode._WHITE) RelatedByLinkTwinMode showRelatedByLinkTwinMode,
            @RequestParam(name = RestRequestParam.showRelatedByHeadTwinMode, defaultValue = RelatedByHeadTwinMode._WHITE) RelatedByHeadTwinMode showRelatedByHeadTwinMode,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._SHORT) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._SHORT) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestParam(name = RestRequestParam.showClassMode, defaultValue = TwinClassBaseRestDTOMapper.ClassMode._SHORT) TwinClassBaseRestDTOMapper.ClassMode showClassMode,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @RequestParam(name = RestRequestParam.showClassStatusMode, defaultValue = TwinClassRestDTOMapper.StatusMode._HIDE) TwinClassRestDTOMapper.StatusMode showClassStatusMode,
            @RequestParam(name = RestRequestParam.showClassMarkerMode, defaultValue = TwinClassRestDTOMapper.MarkerMode._HIDE) TwinClassRestDTOMapper.MarkerMode showClassMarkerMode,
            @RequestParam(name = RestRequestParam.showClassTagMode, defaultValue = TwinClassRestDTOMapper.TagMode._HIDE) TwinClassRestDTOMapper.TagMode showClassTagMode,
            @RequestParam(name = RestRequestParam.showTwinMode, defaultValue = TwinBaseRestDTOMapper.TwinMode._DETAILED) TwinBaseRestDTOMapper.TwinMode showTwinMode,
            @RequestParam(name = RestRequestParam.showTwinFieldMode, defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @RequestParam(name = RestRequestParam.showTwinAttachmentMode, defaultValue = AttachmentViewRestDTOMapper.TwinAttachmentMode._ALL) AttachmentViewRestDTOMapper.TwinAttachmentMode showTwinAttachmentMode,
            @RequestParam(name = RestRequestParam.showAttachmentMode, defaultValue = AttachmentViewRestDTOMapper.Mode._HIDE) AttachmentViewRestDTOMapper.Mode showAttachmentMode,
            @RequestParam(name = RestRequestParam.showTwinMarkerMode, defaultValue = TwinBaseV3RestDTOMapper.TwinMarkerMode._HIDE) TwinBaseV3RestDTOMapper.TwinMarkerMode showTwinMarkerMode,
            @RequestParam(name = RestRequestParam.showTwinTagMode, defaultValue = TwinBaseV3RestDTOMapper.TwinTagMode._HIDE) TwinBaseV3RestDTOMapper.TwinTagMode showTwinTagMode,
            @RequestParam(name = RestRequestParam.showTwinLinkMode, defaultValue = TwinLinkRestDTOMapper.Mode._HIDE) TwinLinkRestDTOMapper.Mode showTwinLinkMode,
            @RequestParam(name = RestRequestParam.showLinkMode, defaultValue = LinkRestDTOMapper.Mode._HIDE) LinkRestDTOMapper.Mode showLinkMode,
            @RequestParam(name = RestRequestParam.showTwinTransitionMode, defaultValue = TwinTransitionRestDTOMapper.Mode._HIDE) TwinTransitionRestDTOMapper.Mode showTwinTransitionMode) {
        TwinRsDTOv2 rs = new TwinRsDTOv2();
        try {
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(lazyRelation)
                    .setMode(showRelatedByHeadTwinMode)
                    .setMode(showRelatedByLinkTwinMode)
                    .setMode(showUserMode)
                    .setMode(showStatusMode)
                    .setMode(showClassMode)
                    .setMode(showClassStatusMode)
                    .setMode(showClassMarkerMode)
                    .setMode(showClassTagMode)
                    .setMode(showClassFieldMode)
                    .setMode(showTwinMode)
                    .setMode(showTwinFieldMode)
                    .setMode(showTwinAttachmentMode)
                    .setMode(showAttachmentMode)
                    .setMode(showTwinMarkerMode)
                    .setMode(showTwinTagMode)
                    .setMode(showTwinLinkMode)
                    .setMode(showLinkMode)
                    .setMode(showTwinTransitionMode);
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
    @RequestMapping(value = "/private/twin_by_alias/{twinAlias}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinViewByAliasV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable String twinAlias,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @RequestParam(name = RestRequestParam.showRelatedByLinkTwinMode, defaultValue = RelatedByLinkTwinMode._WHITE) RelatedByLinkTwinMode showRelatedByLinkTwinMode,
            @RequestParam(name = RestRequestParam.showRelatedByHeadTwinMode, defaultValue = RelatedByHeadTwinMode._WHITE) RelatedByHeadTwinMode showRelatedByHeadTwinMode,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._SHORT) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._SHORT) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestParam(name = RestRequestParam.showClassMode, defaultValue = TwinClassBaseRestDTOMapper.ClassMode._SHORT) TwinClassBaseRestDTOMapper.ClassMode showClassMode,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @RequestParam(name = RestRequestParam.showClassStatusMode, defaultValue = TwinClassRestDTOMapper.StatusMode._HIDE) TwinClassRestDTOMapper.StatusMode showClassStatusMode,
            @RequestParam(name = RestRequestParam.showClassMarkerMode, defaultValue = TwinClassRestDTOMapper.MarkerMode._HIDE) TwinClassRestDTOMapper.MarkerMode showClassMarkerMode,
            @RequestParam(name = RestRequestParam.showClassTagMode, defaultValue = TwinClassRestDTOMapper.TagMode._HIDE) TwinClassRestDTOMapper.TagMode showClassTagMode,
            @RequestParam(name = RestRequestParam.showTwinMode, defaultValue = TwinBaseRestDTOMapper.TwinMode._DETAILED) TwinBaseRestDTOMapper.TwinMode showTwinMode,
            @RequestParam(name = RestRequestParam.showTwinFieldMode, defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @RequestParam(name = RestRequestParam.showTwinAttachmentMode, defaultValue = AttachmentViewRestDTOMapper.TwinAttachmentMode._ALL) AttachmentViewRestDTOMapper.TwinAttachmentMode showTwinAttachmentMode,
            @RequestParam(name = RestRequestParam.showAttachmentMode, defaultValue = AttachmentViewRestDTOMapper.Mode._HIDE) AttachmentViewRestDTOMapper.Mode showAttachmentMode,
            @RequestParam(name = RestRequestParam.showTwinMarkerMode, defaultValue = TwinBaseV3RestDTOMapper.TwinMarkerMode._HIDE) TwinBaseV3RestDTOMapper.TwinMarkerMode showTwinMarkerMode,
            @RequestParam(name = RestRequestParam.showTwinTagMode, defaultValue = TwinBaseV3RestDTOMapper.TwinTagMode._HIDE) TwinBaseV3RestDTOMapper.TwinTagMode showTwinTagMode,
            @RequestParam(name = RestRequestParam.showTwinLinkMode, defaultValue = TwinLinkRestDTOMapper.Mode._HIDE) TwinLinkRestDTOMapper.Mode showTwinLinkMode,
            @RequestParam(name = RestRequestParam.showLinkMode, defaultValue = LinkRestDTOMapper.Mode._HIDE) LinkRestDTOMapper.Mode showLinkMode,
            @RequestParam(name = RestRequestParam.showTwinTransitionMode, defaultValue = TwinTransitionRestDTOMapper.Mode._HIDE) TwinTransitionRestDTOMapper.Mode showTwinTransitionMode) {
        TwinRsDTOv1 rs = new TwinRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(lazyRelation)
                    .setMode(showRelatedByHeadTwinMode)
                    .setMode(showRelatedByLinkTwinMode)
                    .setMode(showUserMode)
                    .setMode(showStatusMode)
                    .setMode(showClassMode)
                    .setMode(showClassFieldMode)
                    .setMode(showClassStatusMode)
                    .setMode(showClassMarkerMode)
                    .setMode(showClassTagMode)
                    .setMode(showTwinMode)
                    .setMode(showTwinFieldMode)
                    .setMode(showTwinAttachmentMode)
                    .setMode(showAttachmentMode)
                    .setMode(showTwinMarkerMode)
                    .setMode(showTwinTagMode)
                    .setMode(showTwinLinkMode)
                    .setMode(showLinkMode)
                    .setMode(showTwinTransitionMode);
            rs
                    .twin(twinRestDTOMapper.convert(twinService.findTwinByAlias(apiUser, twinAlias), mapperContext))
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
    @RequestMapping(value = "/private/twin_by_alias/{twinAlias}/v2", method = RequestMethod.GET)
    public ResponseEntity<?> twinViewByAliasV2(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable String twinAlias,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @RequestParam(name = RestRequestParam.showRelatedByLinkTwinMode, defaultValue = RelatedByLinkTwinMode._WHITE) RelatedByLinkTwinMode showRelatedByLinkTwinMode,
            @RequestParam(name = RestRequestParam.showRelatedByHeadTwinMode, defaultValue = RelatedByHeadTwinMode._WHITE) RelatedByHeadTwinMode showRelatedByHeadTwinMode,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._SHORT) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._SHORT) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestParam(name = RestRequestParam.showClassMode, defaultValue = TwinClassBaseRestDTOMapper.ClassMode._SHORT) TwinClassBaseRestDTOMapper.ClassMode showClassMode,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @RequestParam(name = RestRequestParam.showClassStatusMode, defaultValue = TwinClassRestDTOMapper.StatusMode._HIDE) TwinClassRestDTOMapper.StatusMode showClassStatusMode,
            @RequestParam(name = RestRequestParam.showClassMarkerMode, defaultValue = TwinClassRestDTOMapper.MarkerMode._HIDE) TwinClassRestDTOMapper.MarkerMode showClassMarkerMode,
            @RequestParam(name = RestRequestParam.showClassTagMode, defaultValue = TwinClassRestDTOMapper.TagMode._HIDE) TwinClassRestDTOMapper.TagMode showClassTagMode,
            @RequestParam(name = RestRequestParam.showTwinMode, defaultValue = TwinBaseRestDTOMapper.TwinMode._DETAILED) TwinBaseRestDTOMapper.TwinMode showTwinMode,
            @RequestParam(name = RestRequestParam.showTwinFieldMode, defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @RequestParam(name = RestRequestParam.showTwinAttachmentMode, defaultValue = AttachmentViewRestDTOMapper.TwinAttachmentMode._ALL) AttachmentViewRestDTOMapper.TwinAttachmentMode showTwinAttachmentMode,
            @RequestParam(name = RestRequestParam.showAttachmentMode, defaultValue = AttachmentViewRestDTOMapper.Mode._HIDE) AttachmentViewRestDTOMapper.Mode showAttachmentMode,
            @RequestParam(name = RestRequestParam.showTwinMarkerMode, defaultValue = TwinBaseV3RestDTOMapper.TwinMarkerMode._HIDE) TwinBaseV3RestDTOMapper.TwinMarkerMode showTwinMarkerMode,
            @RequestParam(name = RestRequestParam.showTwinTagMode, defaultValue = TwinBaseV3RestDTOMapper.TwinTagMode._HIDE) TwinBaseV3RestDTOMapper.TwinTagMode showTwinTagMode,
            @RequestParam(name = RestRequestParam.showTwinLinkMode, defaultValue = TwinLinkRestDTOMapper.Mode._HIDE) TwinLinkRestDTOMapper.Mode showTwinLinkMode,
            @RequestParam(name = RestRequestParam.showLinkMode, defaultValue = LinkRestDTOMapper.Mode._HIDE) LinkRestDTOMapper.Mode showLinkMode,
            @RequestParam(name = RestRequestParam.showTwinTransitionMode, defaultValue = TwinTransitionRestDTOMapper.Mode._HIDE) TwinTransitionRestDTOMapper.Mode showTwinTransitionMode) {
        TwinRsDTOv1 rs = new TwinRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(lazyRelation)
                    .setMode(showRelatedByHeadTwinMode)
                    .setMode(showRelatedByLinkTwinMode)
                    .setMode(showUserMode)
                    .setMode(showStatusMode)
                    .setMode(showClassMode)
                    .setMode(showClassFieldMode)
                    .setMode(showClassStatusMode)
                    .setMode(showClassMarkerMode)
                    .setMode(showClassTagMode)
                    .setMode(showTwinMode)
                    .setMode(showTwinFieldMode)
                    .setMode(showTwinAttachmentMode)
                    .setMode(showAttachmentMode)
                    .setMode(showTwinMarkerMode)
                    .setMode(showTwinTagMode)
                    .setMode(showTwinLinkMode)
                    .setMode(showLinkMode)
                    .setMode(showTwinTransitionMode);
            rs
                    .twin(twinRestDTOMapper.convert(twinService.findTwinByAlias(apiUser, twinAlias), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
