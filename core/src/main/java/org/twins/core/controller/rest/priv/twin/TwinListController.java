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
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinSearchByAliasRqDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRqDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRsDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRsDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twin.TwinSearchByAliasDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinSearchWithHeadDTOReverseMapper;
import org.twins.core.service.twin.TwinSearchResult;
import org.twins.core.service.twin.TwinSearchService;

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
    private final TwinSearchService twinSearchService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final TwinRestDTOMapper twinRestDTOMapper;
    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    private final TwinSearchWithHeadDTOReverseMapper twinSearchWithHeadDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final TwinSearchByAliasDTOReverseMapper twinSearchByAliasDTOReverseMapper;

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
            @MapperContextBinding(roots = TwinRestDTOMapper.class, response = TwinSearchRsDTOv1.class) MapperContext mapperContext,
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
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinSearchRsDTOv2.class) MapperContext mapperContext,
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
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinSearchRsDTOv2.class) MapperContext mapperContext,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit,
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
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinSearchRsDTOv2.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.SEARCH_ALIAS) @PathVariable String searchAlias,  //todo not use
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
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinSearchRsDTOv2.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.SEARCH_ID) @PathVariable UUID searchId,
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
