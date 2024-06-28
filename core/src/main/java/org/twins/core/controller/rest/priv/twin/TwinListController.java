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
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.MapperModeParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinSearchByAliasRqDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRqDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRsDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRsDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.*;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinSearchResult;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.cambium.common.util.PaginationUtils.DEFAULT_VALUE_LIMIT;
import static org.cambium.common.util.PaginationUtils.DEFAULT_VALUE_OFFSET;

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
    final TwinSearchWithHeadDTOReverseMapper twinSearchWithHeadDTOReverseMapper;
    final PaginationMapper paginationMapper;
    final TwinSearchByAliasDTOReverseMapper twinSearchByAliasDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinSearchV1", summary = "Twins basic search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/search/v1")
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> twinSearchV1(
            MapperContext mapperContext,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @MapperModeParam MapperMode.TwinByLinkMode showRelatedByLinkTwinMode,
            @MapperModeParam MapperMode.TwinByHeadMode showRelatedByHeadTwinMode,
            @MapperModeParam(def = MapperMode.AssigneeMode.Fields.SHORT) MapperMode.AssigneeMode showAssigneeMode,
            @MapperModeParam(def = MapperMode.CreatorMode.Fields.SHORT) MapperMode.CreatorMode showCreatorMode,
            @MapperModeParam(def = MapperMode.OwnerMode.Fields.SHORT) MapperMode.OwnerMode showOwnerMode,
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
            @MapperModeParam MapperMode.TwinMarkerMode showTwinMarkerMode,
            @MapperModeParam MapperMode.TwinTagMode showTwinTagMode,
            @MapperModeParam MapperMode.TwinAliasMode showTwinAliasMode,
            @MapperModeParam MapperMode.TwinLinkMode showTwinLinkMode,
            @MapperModeParam MapperMode.TwinLinkOnLinkMode showTwinLinkOnLinkMode,
            @MapperModeParam MapperMode.TwinTransitionMode showTwinTransitionMode,
            @MapperModeParam MapperMode.TwinActionMode showTwinActionMode,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit,
            @RequestBody TwinSearchRqDTOv1 request) {
        TwinSearchRsDTOv1 rs = new TwinSearchRsDTOv1();
        try {
            TwinSearchResult twinSearchResult = twinSearchService.findTwins(twinSearchWithHeadDTOReverseMapper.convert(request), offset, limit);
            rs
                    .setTwinList(twinRestDTOMapper.convertCollection(twinSearchResult.getTwinList(), mapperContext))
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
    @Operation(operationId = "twinSearchV2", summary = "Twins basic search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/search/v2")
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> twinSearchV2(
            MapperContext mapperContext,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @MapperModeParam MapperMode.TwinByLinkMode showRelatedByLinkTwinMode,
            @MapperModeParam MapperMode.TwinByHeadMode showRelatedByHeadTwinMode,
            @MapperModeParam(def = MapperMode.AssigneeMode.Fields.SHORT) MapperMode.AssigneeMode showAssigneeMode,
            @MapperModeParam(def = MapperMode.CreatorMode.Fields.SHORT) MapperMode.CreatorMode showCreatorMode,
            @MapperModeParam(def = MapperMode.OwnerMode.Fields.SHORT) MapperMode.OwnerMode showOwnerMode,
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
            @MapperModeParam MapperMode.TwinMarkerMode showTwinMarkerMode,
            @MapperModeParam MapperMode.TwinTagMode showTwinTagMode,
            @MapperModeParam MapperMode.TwinAliasMode showTwinAliasMode,
            @MapperModeParam MapperMode.TwinLinkMode showTwinLinkMode,
            @MapperModeParam MapperMode.TwinLinkOnLinkMode showTwinLinkOnLinkMode,
            @MapperModeParam MapperMode.TwinTransitionMode showTwinTransitionMode,
            @MapperModeParam MapperMode.TwinActionMode showTwinActionMode,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit,
            @RequestBody TwinSearchRqDTOv1 request) {
        TwinSearchRsDTOv2 rs = new TwinSearchRsDTOv2();
        try {
            TwinSearchResult twinSearchResult = twinSearchService.findTwins(twinSearchWithHeadDTOReverseMapper.convert(request), offset, limit);
            rs
                    .setTwinList(twinRestDTOMapperV2.convertCollection(twinSearchResult.getTwinList(), mapperContext))
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
    @PostMapping(value = "/private/twin/search/v3")
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> twinSearchV3(
            MapperContext mapperContext,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @MapperModeParam MapperMode.TwinByLinkMode showRelatedByLinkTwinMode,
            @MapperModeParam MapperMode.TwinByHeadMode showRelatedByHeadTwinMode,
            @MapperModeParam(def = MapperMode.AssigneeMode.Fields.SHORT) MapperMode.AssigneeMode showAssigneeMode,
            @MapperModeParam(def = MapperMode.CreatorMode.Fields.SHORT) MapperMode.CreatorMode showCreatorMode,
            @MapperModeParam(def = MapperMode.OwnerMode.Fields.SHORT) MapperMode.OwnerMode showOwnerMode,
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
            @MapperModeParam MapperMode.TwinMarkerMode showTwinMarkerMode,
            @MapperModeParam MapperMode.TwinTagMode showTwinTagMode,
            @MapperModeParam MapperMode.TwinAliasMode showTwinAliasMode,
            @MapperModeParam MapperMode.TwinLinkMode showTwinLinkMode,
            @MapperModeParam MapperMode.TwinLinkOnLinkMode showTwinLinkOnLinkMode,
            @MapperModeParam MapperMode.TwinTransitionMode showTwinTransitionMode,
            @MapperModeParam MapperMode.TwinActionMode showTwinActionMode,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = "0") int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = "10") int limit,
            @RequestBody List<TwinSearchRqDTOv1> request) {
        TwinSearchRsDTOv2 rs = new TwinSearchRsDTOv2();
        try {
            List<BasicSearch> basicSearches = new ArrayList<>();
            for (TwinSearchRqDTOv1 dto : request)
                basicSearches.add(twinSearchWithHeadDTOReverseMapper.convert(dto));
            TwinSearchResult twinSearchResult = twinSearchService.findTwins(basicSearches, offset, limit);
            rs
                    .setTwinList(twinRestDTOMapperV2.convertCollection(twinSearchResult.getTwinList(), mapperContext))
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
    @Operation(operationId = "twinSearchByAliasV1", summary = "Twins search by alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/search_by_alias/{searchAlias}/v1")
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> twinSearchByAliasV1(
            MapperContext mapperContext,
            @Parameter(example = DTOExamples.SEARCH_ALIAS) @PathVariable String searchAlias,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @MapperModeParam MapperMode.TwinByLinkMode showRelatedByLinkTwinMode,
            @MapperModeParam MapperMode.TwinByHeadMode showRelatedByHeadTwinMode,
            @MapperModeParam(def = MapperMode.AssigneeMode.Fields.SHORT) MapperMode.AssigneeMode showAssigneeMode,
            @MapperModeParam(def = MapperMode.CreatorMode.Fields.SHORT) MapperMode.CreatorMode showCreatorMode,
            @MapperModeParam(def = MapperMode.OwnerMode.Fields.SHORT) MapperMode.OwnerMode showOwnerMode,
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
            @MapperModeParam MapperMode.TwinMarkerMode showTwinMarkerMode,
            @MapperModeParam MapperMode.TwinTagMode showTwinTagMode,
            @MapperModeParam MapperMode.TwinAliasMode showTwinAliasMode,
            @MapperModeParam MapperMode.TwinLinkMode showTwinLinkMode,
            @MapperModeParam MapperMode.TwinLinkOnLinkMode showTwinLinkOnLinkMode,
            @MapperModeParam MapperMode.TwinTransitionMode showTwinTransitionMode,
            @MapperModeParam MapperMode.TwinActionMode showTwinActionMode,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit,
            @RequestBody TwinSearchByAliasRqDTOv1 request) {
        TwinSearchRsDTOv2 rs = new TwinSearchRsDTOv2();
        try {
            TwinSearchResult twinSearchResult = twinSearchService.findTwins(twinSearchByAliasDTOReverseMapper.convert(request), offset, limit);
            rs
                    .setTwinList(twinRestDTOMapperV2.convertCollection(twinSearchResult.getTwinList(), mapperContext))
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
    @Operation(operationId = "twinSearchByIdV1", summary = "Twins search by search_id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/search/{searchId}/v1")
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> twinSearchByIdV1(
            MapperContext mapperContext,
            @Parameter(example = DTOExamples.SEARCH_ID) @PathVariable UUID searchId,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @MapperModeParam MapperMode.TwinByLinkMode showRelatedByLinkTwinMode,
            @MapperModeParam MapperMode.TwinByHeadMode showRelatedByHeadTwinMode,
            @MapperModeParam(def = MapperMode.AssigneeMode.Fields.SHORT) MapperMode.AssigneeMode showAssigneeMode,
            @MapperModeParam(def = MapperMode.CreatorMode.Fields.SHORT) MapperMode.CreatorMode showCreatorMode,
            @MapperModeParam(def = MapperMode.OwnerMode.Fields.SHORT) MapperMode.OwnerMode showOwnerMode,
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
            @MapperModeParam MapperMode.TwinMarkerMode showTwinMarkerMode,
            @MapperModeParam MapperMode.TwinTagMode showTwinTagMode,
            @MapperModeParam MapperMode.TwinAliasMode showTwinAliasMode,
            @MapperModeParam MapperMode.TwinLinkMode showTwinLinkMode,
            @MapperModeParam MapperMode.TwinLinkOnLinkMode showTwinLinkOnLinkMode,
            @MapperModeParam MapperMode.TwinTransitionMode showTwinTransitionMode,
            @MapperModeParam MapperMode.TwinActionMode showTwinActionMode,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit,
            @RequestBody TwinSearchByAliasRqDTOv1 request) {
        TwinSearchRsDTOv2 rs = new TwinSearchRsDTOv2();
        try {
            TwinSearchResult twinSearchResult = twinSearchService.findTwins(searchId, request.getParams(), twinSearchWithHeadDTOReverseMapper.convert(request.getNarrow()), offset, limit);
            rs
                    .setTwinList(twinRestDTOMapperV2.convertCollection(twinSearchResult.getTwinList(), mapperContext))
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
