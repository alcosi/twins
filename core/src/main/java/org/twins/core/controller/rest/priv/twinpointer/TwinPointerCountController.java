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
import org.twins.core.domain.CountResult;
import org.twins.core.domain.twin.TwinPointerSearch;
import org.twins.core.dto.rest.twinpointer.TwinPointerCountRqDTOv1;
import org.twins.core.dto.rest.twinpointer.TwinPointerCountRsDTOv1;
import org.twins.core.enums.sort.TwinPointerGroupField;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinpointer.TwinPointerCountRestDTOMapper;
import org.twins.core.mappers.rest.twinpointer.TwinPointerSearchDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinPointerSearchService;

@Tag(name = ApiTag.TWIN_POINTER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_POINTER_MANAGE, Permissions.TWIN_POINTER_VIEW})
public class TwinPointerCountController extends ApiController {
    private final TwinPointerSearchService twinPointerSearchService;
    private final TwinPointerCountRestDTOMapper twinPointerCountRestDTOMapper;
    private final TwinPointerSearchDTOReverseMapper twinPointerSearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinPointerCountV1", summary = "Count twin pointers by group fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin pointer count", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinPointerCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_pointer/search/count/v1")
    public ResponseEntity<?> twinPointerCountV1(
            @MapperContextBinding(roots = TwinPointerCountRestDTOMapper.class, response = TwinPointerCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinPointerCountRqDTOv1 request) {
        TwinPointerCountRsDTOv1 rs = new TwinPointerCountRsDTOv1();
        try {
            TwinPointerSearch search = twinPointerSearchDTOReverseMapper.convert(request.getSearch());
            PaginationResult<CountResult<TwinPointerEntity, TwinPointerGroupField>> result =
                    twinPointerSearchService.countByGroupFields(search, request.getGroupFields(), pagination);
            rs
                    .setCounts(twinPointerCountRestDTOMapper.convertCollection(result.getList(), mapperContext))
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
