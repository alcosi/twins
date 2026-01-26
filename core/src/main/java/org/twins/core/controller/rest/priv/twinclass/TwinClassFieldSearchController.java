package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TwinClassFieldSearchConfiguredRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldSearchRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldSearchRqDTOv2;
import org.twins.core.dto.rest.twinclass.TwinClassFieldSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldSearchDTOReverseMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldSearchRqDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFieldSearchService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE, Permissions.TWIN_CLASS_VIEW})
public class TwinClassFieldSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final TwinClassFieldSearchRqDTOReverseMapper twinClassFieldSearchRqDTOReverseMapper;
    private final TwinClassFieldSearchDTOReverseMapper twinClassFieldSearchDTOReverseMapper;
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    private final TwinClassFieldSearchService twinClassFieldSearchService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @Deprecated
    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldSearchV1", summary = "Return a list of all twin class field for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_fields/search/v1")
    public ResponseEntity<?> twinClassFieldSearchV1(
            @MapperContextBinding(roots = TwinClassFieldRestDTOMapper.class, response = TwinClassFieldSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinClassFieldSearchRqDTOv1 request) {
        TwinClassFieldSearchRsDTOv1 rs = new TwinClassFieldSearchRsDTOv1();
        try {
            PaginationResult<TwinClassFieldEntity> twinClassFieldList = twinClassFieldSearchService
                    .findTwinClassField(twinClassFieldSearchRqDTOReverseMapper.convert(request), pagination);
            rs
                    .setFields(twinClassFieldRestDTOMapper.convertCollection(twinClassFieldList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twinClassFieldList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldSearchV2", summary = "Return a list of all twin class field for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_fields/search/v2")
    public ResponseEntity<?> twinClassFieldSearchV2(
            @MapperContextBinding(roots = TwinClassFieldRestDTOMapper.class, response = TwinClassFieldSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinClassFieldSearchRqDTOv2 request) {
        TwinClassFieldSearchRsDTOv1 rs = new TwinClassFieldSearchRsDTOv1();
        try {
            PaginationResult<TwinClassFieldEntity> twinClassFieldList = twinClassFieldSearchService
                    .findTwinClassField(twinClassFieldSearchDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setFields(twinClassFieldRestDTOMapper.convertCollection(twinClassFieldList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twinClassFieldList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldSearchConfiguredV1", summary = "Twin class field search by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_fields/search/{searchId}/v1")
    public ResponseEntity<?> twinClassFieldSearchConfiguredV1(
            @MapperContextBinding(roots = TwinClassFieldRestDTOMapper.class, response = TwinClassFieldSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @Parameter(example = DTOExamples.SEARCH_ID) @PathVariable UUID searchId,
            @RequestBody TwinClassFieldSearchConfiguredRqDTOv1 request) {
        TwinClassFieldSearchRsDTOv1 rs = new TwinClassFieldSearchRsDTOv1();
        try {
            PaginationResult<TwinClassFieldEntity> twinClassFieldList = twinClassFieldSearchService
                    .findTwinClassField(searchId, request.getParams(), twinClassFieldSearchDTOReverseMapper.convert(request.getNarrow()), pagination);
            rs
                    .setFields(twinClassFieldRestDTOMapper.convertCollection(twinClassFieldList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twinClassFieldList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
