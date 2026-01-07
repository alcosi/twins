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
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientListRsDTOv1;
import org.twins.core.domain.notification.HistoryNotificationRecipientUpdate;
import org.twins.core.dto.rest.notification.HistoryNotificationRecipientUpdateRqDTOv1;
import org.twins.core.mappers.rest.history.HistoryDTOMapperV1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.notification.HistoryNotificationRecipientDTOMapperV1;
import org.twins.core.mappers.rest.notification.HistoryNotificationRecipientUpdateDTOReverseMapper;
import org.twins.core.service.notification.HistoryNotificationRecipientService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(description = "", name = ApiTag.HISTORY_NOTIFICATION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.HISTORY_NOTIFICATION_MANAGE, Permissions.HISTORY_NOTIFICATION_UPDATE, Permissions.HISTORY_NOTIFICATION_VIEW})
public class HistoryNotificationRecipientUpdateController extends ApiController {
    private final HistoryNotificationRecipientUpdateDTOReverseMapper historyNotificationRecipientUpdateDTOReverseMapper;
    private final HistoryNotificationRecipientDTOMapperV1 historyNotificationRecipientDTOMapperV1;
    private final HistoryNotificationRecipientService historyNotificationRecipientService;

    @ParametersApiUserHeaders
    @Operation(operationId = "historyNotificationRecipientUpdateV1", summary = "Update history notification recipient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = HistoryNotificationRecipientListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/history_notification_recipient/v1")
    public ResponseEntity<?> historyNotificationRecipientV1(
            @MapperContextBinding(roots = HistoryDTOMapperV1.class, response = HistoryNotificationRecipientListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody HistoryNotificationRecipientUpdateRqDTOv1 request) {
        HistoryNotificationRecipientListRsDTOv1 rs = new HistoryNotificationRecipientListRsDTOv1();
        try {
            List<HistoryNotificationRecipientUpdate> historyNotificationRecipientEntityList = historyNotificationRecipientUpdateDTOReverseMapper.convertCollection(request.getRecipients());
            List<HistoryNotificationRecipientEntity> historyNotificationRecipientList = historyNotificationRecipientService.updateHistoryNotificationRecipients(historyNotificationRecipientEntityList);
            rs
                    .setRecipients(historyNotificationRecipientDTOMapperV1.convertCollection(historyNotificationRecipientList, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
