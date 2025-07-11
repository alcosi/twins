package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
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
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassSearchRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassSearchService;
import org.twins.core.service.twinclass.TwinClassService;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE, Permissions.TWIN_CLASS_VIEW})
public class TwinClassListController extends ApiController {
    private final TwinClassService twinClassService;
    private final TwinClassSearchService twinClassSearchService;
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final TwinClassSearchRestDTOReverseMapper twinClassSearchRestDTOReverseMapper;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassSearchV1", summary = "Returns twin class search result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class/search/v1")
    public ResponseEntity<?> twinClassSearchV1(
            @MapperContextBinding(roots = TwinClassRestDTOMapper.class, response = TwinClassSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams(sortField = TwinClassEntity.Fields.key) SimplePagination pagination,
            @RequestBody TwinClassSearchRqDTOv1 request) {
        TwinClassSearchRsDTOv1 rs = new TwinClassSearchRsDTOv1();
        try {
            PaginationResult<TwinClassEntity> twinClasses = twinClassSearchService
                    .findTwinClasses(twinClassSearchRestDTOReverseMapper.convert(request), pagination);
            rs
                    .setTwinClassList(twinClassRestDTOMapper.convertCollection(twinClasses.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twinClasses))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassListV1", summary = "Returns twin class list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin_class/list/v1")
    public ResponseEntity<?> twinClassLstV1(
            @MapperContextBinding(roots = TwinClassRestDTOMapper.class, response = TwinClassSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination) {
        TwinClassSearchRsDTOv1 rs = new TwinClassSearchRsDTOv1();
        try {
            PaginationResult<TwinClassEntity> twinClasses = twinClassSearchService.findTwinClasses(null, pagination);
            rs
                    .setTwinClassList(twinClassRestDTOMapper.convertCollection(twinClasses.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twinClasses))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
