package org.twins.core.controller.rest.priv.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
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
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.domain.notification.HistoryNotificationCreate;
import org.twins.core.dto.rest.notification.HistoryNotificationCreateRqDTOv1;
import org.twins.core.dto.rest.notification.HistoryNotificationListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.notification.HistoryNotificationDTOMapperV1;
import org.twins.core.mappers.rest.notification.HistoryNotificationCreateDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.notification.HistoryNotificationService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(description = "", name = ApiTag.HISTORY_NOTIFICATION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.HISTORY_NOTIFICATION_MANAGE, Permissions.HISTORY_NOTIFICATION_CREATE})
public class HistoryNotificationCreateController extends ApiController {

    private final HistoryNotificationService historyNotificationService;
    private final HistoryNotificationDTOMapperV1 historyNotificationListRsDTOMapper;
    private final HistoryNotificationCreateDTOReverseMapper historyNotificationCreateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "historyNotificationCreateV1", summary = "Create batch history notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The history notification batch was created successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = HistoryNotificationListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/history_notification/v1")
    public ResponseEntity<?> historyNotificationCreateV1(
            @MapperContextBinding(roots = HistoryNotificationDTOMapperV1.class, response = HistoryNotificationListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody HistoryNotificationCreateRqDTOv1 request) {
        HistoryNotificationListRsDTOv1 rs = new HistoryNotificationListRsDTOv1();
        try {
            List<HistoryNotificationCreate> createList = historyNotificationCreateDTOReverseMapper.convertCollection(request.getHistoryNotifications());
            List<HistoryNotificationEntity> historyNotificationList = historyNotificationService.createHistoryNotification(createList);
            rs
                    .setHistoryNotifications(historyNotificationListRsDTOMapper.convertCollection(historyNotificationList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
