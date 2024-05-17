package org.twins.core.controller.rest.priv.twin;

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
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.TwinSearchRqDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRsDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRsDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkRestDTOMapper;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.*;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinTransitionRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinSearchResult;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;

import java.util.ArrayList;
import java.util.List;

import static org.cambium.common.util.PaginationUtils.*;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinListController extends ApiController {
    final AuthService authService;
    final TwinService twinService;
    final TwinSearchService twinSearchService;
    final TwinClassRestDTOMapper twinClassRestDTOMapper;
    final UserRestDTOMapper userRestDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    final TwinRestDTOMapper twinRestDTOMapper;
    final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    final TwinSearchRqDTOMapper twinSearchRqDTOMapper;
    final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinSearchV1", summary = "Twins basic search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/search/v1", method = RequestMethod.POST)
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> twinSearchV1(
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @RequestParam(name = RestRequestParam.showRelatedByLinkTwinMode, defaultValue = RelatedByLinkTwinMode._WHITE) RelatedByLinkTwinMode showRelatedByLinkTwinMode,
            @RequestParam(name = RestRequestParam.showRelatedByHeadTwinMode, defaultValue = RelatedByHeadTwinMode._WHITE) RelatedByHeadTwinMode showRelatedByHeadTwinMode,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._SHORT) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._SHORT) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestParam(name = RestRequestParam.showClassMode, defaultValue = TwinClassBaseRestDTOMapper.ClassMode._SHORT) TwinClassBaseRestDTOMapper.ClassMode showClassMode,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
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
            @RequestParam(name = RestRequestParam.showTwinTransitionMode, defaultValue = TwinTransitionRestDTOMapper.Mode._HIDE) TwinTransitionRestDTOMapper.Mode showTwinTransitionMode,
            @RequestParam(name = RestRequestParam.showTwinActionMode, defaultValue = TwinBaseV3RestDTOMapper.TwinActionMode._HIDE) TwinBaseV3RestDTOMapper.TwinActionMode showTwinActionMode,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit,
            @RequestBody TwinSearchRqDTOv1 request) {
        TwinSearchRsDTOv1 rs = new TwinSearchRsDTOv1();
        try {
            TwinSearchResult twinSearchResult = twinSearchService.findTwins(twinSearchRqDTOMapper.convert(request), createSimplePagination(offset, limit, sort(false, TwinEntity.Fields.createdAt)));
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(lazyRelation)
                    .setMode(showRelatedByHeadTwinMode)
                    .setMode(showRelatedByLinkTwinMode)
                    .setMode(showUserMode)
                    .setMode(showStatusMode)
                    .setMode(showClassMode)
                    .setMode(showClassFieldMode)
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
                    .setMode(showTwinTransitionMode)
                    .setMode(showTwinActionMode);
            rs
                    .setTwinList(twinRestDTOMapper.convertList(twinSearchResult.getTwinList(), mapperContext))
                    .setPagination(paginationMapper.convert(twinSearchResult))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            se.printStackTrace();
            return createErrorRs(se, rs);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinSearchV2", summary = "Twins basic search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/search/v2", method = RequestMethod.POST)
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> twinSearchV2(
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @RequestParam(name = RestRequestParam.showRelatedByLinkTwinMode, defaultValue = RelatedByLinkTwinMode._WHITE) RelatedByLinkTwinMode showRelatedByLinkTwinMode,
            @RequestParam(name = RestRequestParam.showRelatedByHeadTwinMode, defaultValue = RelatedByHeadTwinMode._WHITE) RelatedByHeadTwinMode showRelatedByHeadTwinMode,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._SHORT) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._SHORT) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestParam(name = RestRequestParam.showClassMode, defaultValue = TwinClassBaseRestDTOMapper.ClassMode._SHORT) TwinClassBaseRestDTOMapper.ClassMode showClassMode,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
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
            @RequestParam(name = RestRequestParam.showTwinTransitionMode, defaultValue = TwinTransitionRestDTOMapper.Mode._HIDE) TwinTransitionRestDTOMapper.Mode showTwinTransitionMode,
            @RequestParam(name = RestRequestParam.showTwinActionMode, defaultValue = TwinBaseV3RestDTOMapper.TwinActionMode._HIDE) TwinBaseV3RestDTOMapper.TwinActionMode showTwinActionMode,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit,
            @RequestBody TwinSearchRqDTOv1 request) {
        TwinSearchRsDTOv2 rs = new TwinSearchRsDTOv2();
        try {
            TwinSearchResult twinSearchResult = twinSearchService.findTwins(twinSearchRqDTOMapper.convert(request), createSimplePagination(offset, limit, sort(false, TwinEntity.Fields.createdAt)));
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(lazyRelation)
                    .setMode(showRelatedByHeadTwinMode)
                    .setMode(showRelatedByLinkTwinMode)
                    .setMode(showUserMode)
                    .setMode(showStatusMode)
                    .setMode(showClassMode)
                    .setMode(showClassFieldMode)
                    .setMode(showTwinMode)
                    .setMode(showTwinFieldMode)
                    .setMode(showClassMarkerMode)
                    .setMode(showClassTagMode)
                    .setMode(showTwinAttachmentMode)
                    .setMode(showAttachmentMode)
                    .setMode(showTwinMarkerMode)
                    .setMode(showTwinTagMode)
                    .setMode(showTwinLinkMode)
                    .setMode(showLinkMode)
                    .setMode(showTwinTransitionMode)
                    .setMode(showTwinActionMode);
            rs
                    .setTwinList(twinRestDTOMapperV2.convertList(twinSearchResult.getTwinList(), mapperContext))
                    .setPagination(paginationMapper.convert(twinSearchResult))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinSearchV3", summary = "Twins basic search for several queries connected by OR operator")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/search/v3", method = RequestMethod.POST)
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> twinSearchV3(
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @RequestParam(name = RestRequestParam.showRelatedByLinkTwinMode, defaultValue = RelatedByLinkTwinMode._WHITE) RelatedByLinkTwinMode showRelatedByLinkTwinMode,
            @RequestParam(name = RestRequestParam.showRelatedByHeadTwinMode, defaultValue = RelatedByHeadTwinMode._WHITE) RelatedByHeadTwinMode showRelatedByHeadTwinMode,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._SHORT) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._SHORT) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestParam(name = RestRequestParam.showClassMode, defaultValue = TwinClassBaseRestDTOMapper.ClassMode._SHORT) TwinClassBaseRestDTOMapper.ClassMode showClassMode,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
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
            @RequestParam(name = RestRequestParam.showTwinTransitionMode, defaultValue = TwinTransitionRestDTOMapper.Mode._HIDE) TwinTransitionRestDTOMapper.Mode showTwinTransitionMode,
            @RequestParam(name = RestRequestParam.showTwinActionMode, defaultValue = TwinBaseV3RestDTOMapper.TwinActionMode._HIDE) TwinBaseV3RestDTOMapper.TwinActionMode showTwinActionMode,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = "0") int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = "10") int limit,
            @RequestBody List<TwinSearchRqDTOv1> request) {
        TwinSearchRsDTOv2 rs = new TwinSearchRsDTOv2();
        try {
            List<BasicSearch> basicSearches = new ArrayList<>();
            for(TwinSearchRqDTOv1 dto : request)
                basicSearches.add(twinSearchRqDTOMapper.convert(dto));
            TwinSearchResult twinSearchResult = twinSearchService.findTwins(basicSearches, createSimplePagination(offset, limit, sort(false, TwinEntity.Fields.createdAt)));
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(lazyRelation)
                    .setMode(showRelatedByHeadTwinMode)
                    .setMode(showRelatedByLinkTwinMode)
                    .setMode(showUserMode)
                    .setMode(showStatusMode)
                    .setMode(showClassMode)
                    .setMode(showClassFieldMode)
                    .setMode(showTwinMode)
                    .setMode(showTwinFieldMode)
                    .setMode(showClassMarkerMode)
                    .setMode(showClassTagMode)
                    .setMode(showTwinAttachmentMode)
                    .setMode(showAttachmentMode)
                    .setMode(showTwinMarkerMode)
                    .setMode(showTwinTagMode)
                    .setMode(showTwinLinkMode)
                    .setMode(showLinkMode)
                    .setMode(showTwinTransitionMode)
                    .setMode(showTwinActionMode);
            rs
                    .setTwinList(twinRestDTOMapperV2.convertList(twinSearchResult.getTwinList(), mapperContext))
                    .setPagination(paginationMapper.convert(twinSearchResult))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
