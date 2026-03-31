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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDynamicMarkerSearchRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDynamicMarkerSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassDynamicMarkerDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassDynamicMarkerSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassDynamicMarkerSearchService;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_DYNAMIC_MARKER_MANAGE, Permissions.TWIN_CLASS_DYNAMIC_MARKER_VIEW})
public class TwinClassDynamicMarkerSearchController extends ApiController {

    private final TwinClassDynamicMarkerSearchRestDTOReverseMapper twinClassDynamicMarkerSearchRestDTOReverseMapper;
    private final TwinClassDynamicMarkerSearchService twinClassDynamicMarkerSearchService;
    private final TwinClassDynamicMarkerDTOMapper twinClassDynamicMarkerDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassDynamicMarkerSearchV1", summary = "Returns twin class dynamic markers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class dynamic markers list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassDynamicMarkerSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_dynamic_marker/search/v1")
    public ResponseEntity<?> twinClassDynamicMarkerSearchV1(
            @MapperContextBinding(roots = TwinClassDynamicMarkerDTOMapper.class, response = TwinClassDynamicMarkerSearchRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinClassDynamicMarkerSearchRqDTOv1 request) {
        TwinClassDynamicMarkerSearchRsDTOv1 rs = new TwinClassDynamicMarkerSearchRsDTOv1();
        try {
            PaginationResult<TwinClassDynamicMarkerEntity> twinClassDynamicMarkerList = twinClassDynamicMarkerSearchService.findTwinClassDynamicMarkers(twinClassDynamicMarkerSearchRestDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setPagination(paginationMapper.convert(twinClassDynamicMarkerList))
                    .setDynamicMarkers(twinClassDynamicMarkerDTOMapper.convertCollection(twinClassDynamicMarkerList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
