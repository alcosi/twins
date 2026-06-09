package org.twins.core.controller.rest.priv.twin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.*;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinSearchServiceV2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_MANAGE, Permissions.TWIN_VIEW})
public class TwinListController extends ApiController {
    private final TwinSearchService twinSearchService;
    private final TwinSearchServiceV2 twinSearchServiceV2;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    private final TwinSearchExtendedDTOReverseMapper twinSearchExtendedDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final TwinSearchByAliasDTOReverseMapper twinSearchByAliasDTOReverseMapper;
    private final TwinSearchRqDTOv2ReverseMapper twinSearchRqDTOv2ReverseMapper;
    private final TwinSearchExtendedDTOv2ReverseMapper twinSearchExtendedDTOv2ReverseMapper;
    private final TwinCountRestDTOMapper twinCountRestDTOMapper;
    private final TwinSortDTOReverseMapperV2 twinSortDTOReverseMapperV2;

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
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinSearchRsDTOv2.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams(sortAsc = false, sortField = TwinEntity.Fields.createdAt) SimplePagination pagination,
            @RequestBody TwinSearchRqDTOv1 request) {
        TwinSearchRsDTOv2 rs = new TwinSearchRsDTOv2();
        try {
            PaginationResult<TwinEntity> twins = twinSearchService.findTwins(twinSearchExtendedDTOReverseMapper.convert(request), pagination);
            rs
                    .setTwinList(twinRestDTOMapperV2.convertCollection(twins.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twins))
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
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinSearchRsDTOv2.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams(sortAsc = false, sortField = TwinEntity.Fields.createdAt) SimplePagination pagination,
            @RequestBody List<TwinSearchRqDTOv1> request) {
        TwinSearchRsDTOv2 rs = new TwinSearchRsDTOv2();
        try {
            List<BasicSearch> basicSearches = new ArrayList<>();
            for (TwinSearchRqDTOv1 dto : request)
                basicSearches.add(twinSearchExtendedDTOReverseMapper.convert(dto));
            PaginationResult<TwinEntity> twins = twinSearchService.findTwins(basicSearches, pagination);
            rs
                    .setTwinList(twinRestDTOMapperV2.convertCollection(twins.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twins))
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
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinSearchRsDTOv2.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.SEARCH_ALIAS) @PathVariable String searchAlias,
            @SimplePaginationParams(sortAsc = false, sortField = TwinEntity.Fields.createdAt) SimplePagination pagination,
            @RequestBody TwinSearchByAliasRqDTOv1 request) {
        TwinSearchRsDTOv2 rs = new TwinSearchRsDTOv2();
        try {
            PaginationResult<TwinEntity> twins = twinSearchService.findTwins(twinSearchByAliasDTOReverseMapper.convert(request).setAlias(searchAlias), pagination);
            rs
                    .setTwinList(twinRestDTOMapperV2.convertCollection(twins.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twins))
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
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinSearchRsDTOv2.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams(sortAsc = false, sortField = TwinEntity.Fields.createdAt) SimplePagination pagination,
            @Parameter(example = DTOExamples.SEARCH_ID) @PathVariable UUID searchId,
            @RequestBody TwinSearchByAliasRqDTOv1 request) {
        TwinSearchRsDTOv2 rs = new TwinSearchRsDTOv2();
        try {
            PaginationResult<TwinEntity> twins = twinSearchService.findTwins(searchId, request.getParams(), twinSearchExtendedDTOReverseMapper.convert(request.getNarrow()), pagination);
            rs
                    .setTwinList(twinRestDTOMapperV2.convertCollection(twins.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twins))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinSearchV4", summary = "Twins search with multi-field sorting via TwinClassFieldId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/search/v4")
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> twinSearchV4(
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinSearchRsDTOv2.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams(sortAsc = false, sortField = TwinEntity.Fields.createdAt) SimplePagination pagination,
            @RequestBody @Valid TwinSearchRqDTOv2 request) {
        TwinSearchRsDTOv2 rs = new TwinSearchRsDTOv2();
        try {
            var basicSearch = twinSearchRqDTOv2ReverseMapper.convert(request);
            var sort = twinSortDTOReverseMapperV2.convert(request.getSorts());
            PaginationResult<TwinEntity> twins = twinSearchServiceV2.search(basicSearch, pagination, sort);
            rs
                    .setTwinList(twinRestDTOMapperV2.convertCollection(twins.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twins))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinCountV1", summary = "Count twins grouped by TwinClassFieldId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count results", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/count/v1")
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> twinCountV1(
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid TwinCountRqDTOv1 request) {
        TwinCountRsDTOv1 rs = new TwinCountRsDTOv1();
        try {
            BasicSearch search = twinSearchExtendedDTOv2ReverseMapper.convert(request.getSearch());
            var results = twinSearchServiceV2.countByGroupFields(search, request.getGroupFields(), pagination);
            rs
                    .setCounts(twinCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(results))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
