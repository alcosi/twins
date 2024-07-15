package org.twins.core.controller.rest.priv.twinflow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.twinflow.TwinflowSearchRqDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TwinflowBaseV3RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowSearchRestDTOReverseMapper;
import org.twins.core.service.twinflow.TwinflowService;

import static org.cambium.common.util.PaginationUtils.*;

@Tag(name = ApiTag.TWINFLOW)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinflowListController extends ApiController {
    private final TwinflowService twinflowService;
    private final TwinflowBaseV3RestDTOMapper twinflowBaseV3RestDTOMapper;
    private final TwinflowSearchRestDTOReverseMapper twinflowSearchRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinflowSearchV1", summary = "Returns twinflow search result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twinflow list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinflowSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twinflow/search/v1")
    public ResponseEntity<?> twinflowSearchV1(
            @MapperContextBinding(roots = TwinflowBaseV3RestDTOMapper.class, response = TwinflowSearchRsDTOv1.class) MapperContext mapperContext,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit,
            @RequestBody TwinflowSearchRqDTOv1 request) {
        TwinflowSearchRsDTOv1 rs = new TwinflowSearchRsDTOv1();
        try {
            PaginationResult<TwinflowEntity> twinflowList = twinflowService
                    .search(twinflowSearchRestDTOReverseMapper.convert(request), createSimplePagination(offset, limit, Sort.unsorted()));
            rs
                    .setTwinflowList(twinflowBaseV3RestDTOMapper
                            .convertCollection(twinflowList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twinflowList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
