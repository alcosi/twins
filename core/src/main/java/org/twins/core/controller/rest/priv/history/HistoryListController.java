package org.twins.core.controller.rest.priv.history;

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
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dto.rest.history.HistorySearchRqDTOv1;
import org.twins.core.dto.rest.history.HistorySearchRsDTOv1;
import org.twins.core.mappers.rest.history.HistoryDTOMapperV1;
import org.twins.core.mappers.rest.history.HistorySearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.history.HistorySearchService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "", name = ApiTag.HISTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.HISTORY_VIEW)
public class HistoryListController extends ApiController {
    private final HistorySearchService historySearchService;
    private final HistoryDTOMapperV1 historyDTOMapperV1;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final HistorySearchDTOReverseMapper historySearchDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "historySearchV1", summary = "Return a list of all histories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = HistorySearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_history/search/v1")
    public ResponseEntity<?> historySearchV1(
            @MapperContextBinding(roots = HistoryDTOMapperV1.class, response = HistorySearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams(sortAsc = false, sortField = HistoryEntity.Fields.createdAt) SimplePagination pagination,
            @RequestBody HistorySearchRqDTOv1 request) {
        HistorySearchRsDTOv1 rs = new HistorySearchRsDTOv1();
        try {
            PaginationResult<HistoryEntity> historyList = historySearchService
                    .findHistory(historySearchDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setPagination(paginationMapper.convert(historyList))
                    .setHistoryList(historyDTOMapperV1.convertCollection(historyList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
