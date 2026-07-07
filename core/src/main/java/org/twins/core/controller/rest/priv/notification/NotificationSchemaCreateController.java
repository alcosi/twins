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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.domain.notification.NotificationSchemaCreate;
import org.twins.core.dto.rest.notification.NotificationSchemaCreateRqDTOv1;
import org.twins.core.dto.rest.notification.NotificationSchemaListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.notification.NotificationSchemaCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.notification.NotificationSchemaRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.notification.NotificationSchemaService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(description = "", name = ApiTag.NOTIFICATION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.NOTIFICATION_SCHEMA_CREATE)
public class NotificationSchemaCreateController extends ApiController {

    private final NotificationSchemaCreateRestDTOReverseMapper notificationSchemaCreateRestDTOReverseMapper;
    private final NotificationSchemaRestDTOMapper notificationSchemaRestDTOMapper;
    private final NotificationSchemaService notificationSchemaService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "notificationSchemaCreateV1", summary = "Create notification schema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification schema created successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = NotificationSchemaListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/notification_schema/v1")
    public ResponseEntity<?> notificationSchemaCreateV1(
            @MapperContextBinding(roots = NotificationSchemaRestDTOMapper.class, response = NotificationSchemaListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody NotificationSchemaCreateRqDTOv1 request) {
        NotificationSchemaListRsDTOv1 rs = new NotificationSchemaListRsDTOv1();
        try {
            List<NotificationSchemaCreate> notificationSchemaCreates = notificationSchemaCreateRestDTOReverseMapper.convertCollection(request.getNotificationSchemas(), mapperContext);
            List<NotificationSchemaEntity> entities = notificationSchemaService.createNotificationSchema(notificationSchemaCreates);
            rs
                    .setNotificationSchemas(notificationSchemaRestDTOMapper.convertCollection(entities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
