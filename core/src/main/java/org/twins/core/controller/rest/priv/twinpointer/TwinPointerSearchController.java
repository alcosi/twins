package org.twins.core.controller.rest.priv.twinpointer;

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
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.domain.twin.TwinPointerSearch;
import org.twins.core.dto.rest.twinpointer.TwinPointerSearchRqDTOv1;
import org.twins.core.dto.rest.twinpointer.TwinPointerSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinpointer.TwinPointerRestDTOMapper;
import org.twins.core.mappers.rest.twinpointer.TwinPointerSearchDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinPointerSearchService;

@Tag(name = ApiTag.TWIN_POINTER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_POINTER_MANAGE, Permissions.TWIN_POINTER_VIEW})
public class TwinPointerSearchController extends ApiController {
    private final TwinPointerSearchService twinPointerSearchService;
    private final TwinPointerRestDTOMapper twinPointerRestDTOMapper;
    private final TwinPointerSearchDTOReverseMapper twinPointerSearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinPointerSearchV1", summary = "Twin pointer search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin pointer list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinPointerSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_pointer/search/v1")
    public ResponseEntity<?> twinPointerSearchV1(
            @MapperContextBinding(roots = TwinPointerRestDTOMapper.class, response = TwinPointerSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinPointerSearchRqDTOv1 request) {
        TwinPointerSearchRsDTOv1 rs = new TwinPointerSearchRsDTOv1();
        try {
            TwinPointerSearch search = twinPointerSearchDTOReverseMapper.convert(request.getSearch());
            PaginationResult<TwinPointerEntity> result =
                    twinPointerSearchService.search(search, pagination, request.getSortField(), request.getSortDirection());
            rs
                    .setTwinPointers(twinPointerRestDTOMapper.convertCollection(result.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(result))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
