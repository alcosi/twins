package org.twins.core.controller.rest.priv.notification;

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
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.domain.search.HistoryNotificationSearch;
import org.twins.core.dto.rest.notification.HistoryNotificationSearchRqDTOv1;
import org.twins.core.dto.rest.notification.HistoryNotificationSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.notification.HistoryNotificationDTOMapperV1;
import org.twins.core.mappers.rest.notification.HistoryNotificationSearchDTOReverseMapper;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.notification.HistoryNotificationSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "", name = ApiTag.HISTORY_NOTIFICATION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.HISTORY_NOTIFICATION_MANAGE, Permissions.HISTORY_NOTIFICATION_VIEW})
public class HistoryNotificationSearchController extends ApiController {

    private final HistoryNotificationSearchService historyNotificationSearchService;
    private final HistoryNotificationDTOMapperV1 historyNotificationDTOMapper;
    private final HistoryNotificationSearchDTOReverseMapper historyNotificationSearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "historyNotificationSearchV1", summary = "Search history notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search was successful", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = HistoryNotificationSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/history_notification/search/v1")
    public ResponseEntity<?> historyNotificationSearchV1(
            @MapperContextBinding(roots = HistoryNotificationDTOMapperV1.class, response = HistoryNotificationSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams @Schema(hidden = true) SimplePagination pagination,
            @RequestBody HistoryNotificationSearchRqDTOv1 request) {
        HistoryNotificationSearchRsDTOv1 rs = new HistoryNotificationSearchRsDTOv1();
        try {
            HistoryNotificationSearch search = historyNotificationSearchDTOReverseMapper.convert(request);
            PaginationResult<HistoryNotificationEntity> paginationResult = historyNotificationSearchService.findHistoryNotification(search, pagination);
            rs
                    .setPagination(paginationMapper.convert(paginationResult))
                    .setHistoryNotifications(historyNotificationDTOMapper.convertCollection(paginationResult.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
