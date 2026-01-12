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
import org.twins.core.dao.notification.HistoryNotificationRecipientCollectorEntity;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCollectorSearchRqDTOv1;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCollectorSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.notification.HistoryNotificationRecipientCollectorDTOMapperV1;
import org.twins.core.mappers.rest.notification.HistoryNotificationRecipientCollectorSearchDTOReverseMapper;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.notification.HistoryNotificationRecipientCollectorSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "", name = ApiTag.HISTORY_NOTIFICATION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.HISTORY_NOTIFICATION_MANAGE, Permissions.HISTORY_NOTIFICATION_VIEW})
public class HistoryNotificationRecipientCollectorSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;

    private final HistoryNotificationRecipientCollectorSearchDTOReverseMapper historyNotificationRecipientCollectorSearchDTOReverseMapper;
    private final HistoryNotificationRecipientCollectorSearchService historyNotificationRecipientCollectorSearchService;
    private final HistoryNotificationRecipientCollectorDTOMapperV1 historyNotificationRecipientCollectorDTOMapperV1;

    @ParametersApiUserHeaders
    @Operation(operationId = "historyNotificationRecipientCollectorSearchListV1", summary = "Return a list of all history notification recipient collector for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = HistoryNotificationRecipientCollectorSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/history_notification_recipient_collector/search/v1")
    public ResponseEntity<?> historyNotificationRecipientCollectorSearchListV1(
            @MapperContextBinding(roots = HistoryNotificationRecipientCollectorDTOMapperV1.class, response = HistoryNotificationRecipientCollectorSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody HistoryNotificationRecipientCollectorSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {
        HistoryNotificationRecipientCollectorSearchRsDTOv1 rs = new HistoryNotificationRecipientCollectorSearchRsDTOv1();
        try {
            PaginationResult<HistoryNotificationRecipientCollectorEntity> historyNotificationRecipientCollectorList = historyNotificationRecipientCollectorSearchService
                    .findHistoryNotificationRecipientForDomain(historyNotificationRecipientCollectorSearchDTOReverseMapper.convert(request.getCollector()), pagination);
            rs
                    .setPagination(paginationMapper.convert(historyNotificationRecipientCollectorList))
                    .setRecipientCollectors(historyNotificationRecipientCollectorDTOMapperV1.convertCollection(historyNotificationRecipientCollectorList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
