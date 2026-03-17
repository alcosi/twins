package org.twins.core.controller.rest.priv.user;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.domain.notification.HistoryNotificationUpdate;
import org.twins.core.dto.rest.notification.HistoryNotificationListRsDTOv1;
import org.twins.core.dto.rest.notification.HistoryNotificationUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.notification.HistoryNotificationDTOMapperV1;

import java.util.List;

@Tag(description = "Update user group", name = ApiTag.USER_GROUP)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserGroupUpdateController {

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupUpdateV1", summary = "Update batch history notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The history notification batch was updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = HistoryNotificationListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/user_group/v1")
    public ResponseEntity<?> historyNotificationUpdateV1(
            @MapperContextBinding(roots = HistoryNotificationDTOMapperV1.class, response = HistoryNotificationListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody HistoryNotificationUpdateRqDTOv1 request) {
        HistoryNotificationListRsDTOv1 rs = new HistoryNotificationListRsDTOv1();
        try {
            List<HistoryNotificationUpdate> updateList = historyNotificationUpdateDTOReverseMapper.convertCollection(request.getHistoryNotifications());
            List<HistoryNotificationEntity> historyNotificationList = historyNotificationService.updateHistoryNotification(updateList);
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
