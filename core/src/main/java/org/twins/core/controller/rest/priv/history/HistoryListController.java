package org.twins.core.controller.rest.priv.history;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.SimplePagination;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.history.HistoryListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.history.HistoryDTOMapperV1;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.history.HistoryService;
import org.cambium.common.pagination.PaginationResult;

import java.util.UUID;

import static org.cambium.common.util.PaginationUtils.*;

@Tag(description = "", name = ApiTag.HISTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class HistoryListController extends ApiController {
    private final HistoryService historyService;
    private final HistoryDTOMapperV1 historyDTOMapperV1;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "historyListV1", summary = "Returns twin history by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = HistoryListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin/{twinId}/history/list/v1")
    public ResponseEntity<?> historyListV1(
            @MapperContextBinding(roots = HistoryDTOMapperV1.class, response = HistoryListRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @RequestParam(name = RestRequestParam.childDepth, defaultValue = "0") int childDepth,
            @SimplePaginationParams(sortAsc = false, sortField = HistoryEntity.Fields.createdAt) SimplePagination pagination) {
        HistoryListRsDTOv1 rs = new HistoryListRsDTOv1();
        try {
            PaginationResult<HistoryEntity> historyList = historyService.findHistory(twinId, childDepth, pagination);
            rs
                    .setHistoryList(historyDTOMapperV1.convertCollection(historyList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(historyList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
