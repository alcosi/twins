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
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientSearchRqDTOv1;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.notification.HistoryNotificationRecipientDTOMapperV1;
import org.twins.core.mappers.rest.notification.HistoryNotificationRecipientSearchDTOReverseMapper;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.notification.HistoryNotificationRecipientSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "", name = ApiTag.HISTORY_NOTIFICATION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.HISTORY_NOTIFICATION_MANAGE, Permissions.HISTORY_NOTIFICATION_VIEW})
public class HistoryNotificationRecipientSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;

    private final HistoryNotificationRecipientSearchDTOReverseMapper historyNotificationRecipientSearchDTOReverseMapper;
    private final HistoryNotificationRecipientSearchService historyNotificationRecipientSearchService;
    private final HistoryNotificationRecipientDTOMapperV1 historyNotificationRecipientDTOMapperV1;

    @ParametersApiUserHeaders
    @Operation(operationId = "historyNotificationRecipientSearchListV1", summary = "Return a list of all history notification recipient for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = HistoryNotificationRecipientSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/history_notification_recipient/search/v1")
    public ResponseEntity<?> historyNotificationRecipientSearchListV1(
            @MapperContextBinding(roots = HistoryNotificationRecipientDTOMapperV1.class, response = HistoryNotificationRecipientSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody HistoryNotificationRecipientSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {
        HistoryNotificationRecipientSearchRsDTOv1 rs = new HistoryNotificationRecipientSearchRsDTOv1();
        try {
            PaginationResult<HistoryNotificationRecipientEntity> historyNotificationRecipientList = historyNotificationRecipientSearchService
                    .findHistoryNotificationRecipientForDomain(historyNotificationRecipientSearchDTOReverseMapper.convert(request.getRecipient()), pagination);
            rs
                    .setPagination(paginationMapper.convert(historyNotificationRecipientList))
                    .setRecipients(historyNotificationRecipientDTOMapperV1.convertCollection(historyNotificationRecipientList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
