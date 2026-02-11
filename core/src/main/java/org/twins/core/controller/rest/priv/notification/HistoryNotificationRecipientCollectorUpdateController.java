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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.notification.HistoryNotificationRecipientCollectorEntity;
import org.twins.core.domain.notification.HistoryNotificationRecipientCollectorUpdate;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCollectorListRsDTOv1;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientCollectorUpdateRqDTOv1;
import org.twins.core.mappers.rest.history.HistoryDTOMapperV1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.notification.HistoryNotificationRecipientCollectorDTOMapperV1;
import org.twins.core.mappers.rest.notification.HistoryNotificationRecipientCollectorUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.notification.HistoryNotificationRecipientCollectorService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(description = "", name = ApiTag.HISTORY_NOTIFICATION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.HISTORY_NOTIFICATION_MANAGE, Permissions.HISTORY_NOTIFICATION_UPDATE})
public class HistoryNotificationRecipientCollectorUpdateController extends ApiController {
    private final HistoryNotificationRecipientCollectorService historyNotificationRecipientCollectorService;
    private final HistoryNotificationRecipientCollectorDTOMapperV1 historyNotificationRecipientCollectorListRsDTOMapper;
    private final HistoryNotificationRecipientCollectorUpdateDTOReverseMapper historyNotificationRecipientCollectorUpdateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "historyNotificationRecipientCollectorUpdateV1", summary = "Update history notification recipient collector")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The history notification recipient collector was updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = HistoryNotificationRecipientCollectorListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/history_notification_recipient_collector/v1")
    public ResponseEntity<?> historyNotificationRecipientCollectorUpdateV1(
            @MapperContextBinding(roots = HistoryNotificationRecipientCollectorDTOMapperV1.class, response = HistoryNotificationRecipientCollectorListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody HistoryNotificationRecipientCollectorUpdateRqDTOv1 request) {
        HistoryNotificationRecipientCollectorListRsDTOv1 rs = new HistoryNotificationRecipientCollectorListRsDTOv1();
        try {
            List<HistoryNotificationRecipientCollectorUpdate> updateList = historyNotificationRecipientCollectorUpdateDTOReverseMapper.convertCollection(request.getHistoryNotificationRecipients());
            List<HistoryNotificationRecipientCollectorEntity> historyNotificationRecipientList = historyNotificationRecipientCollectorService.updateRecipientCollectors(updateList);
            rs
                    .setRecipientCollectors(historyNotificationRecipientCollectorListRsDTOMapper.convertCollection(historyNotificationRecipientList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
