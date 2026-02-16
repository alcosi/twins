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
import org.twins.core.dao.notification.HistoryNotificationSchemaMapEntity;
import org.twins.core.domain.notification.HistoryNotificationSchemaMapUpdate;
import org.twins.core.dto.rest.notification.HistoryNotificationSchemaMapListRsDTOv1;
import org.twins.core.dto.rest.notification.HistoryNotificationSchemaMapUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.notification.HistoryNotificationSchemaMapDTOMapperV1;
import org.twins.core.mappers.rest.notification.HistoryNotificationSchemaMapUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.notification.HistoryNotificationSchemaMapService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(description = "", name = ApiTag.HISTORY_NOTIFICATION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.HISTORY_NOTIFICATION_MANAGE, Permissions.HISTORY_NOTIFICATION_UPDATE})
public class HistoryNotificationSchemaMapUpdateController extends ApiController {

    private final HistoryNotificationSchemaMapService historyNotificationSchemaMapService;
    private final HistoryNotificationSchemaMapDTOMapperV1 historyNotificationSchemaMapListRsDTOMapper;
    private final HistoryNotificationSchemaMapUpdateDTOReverseMapper historyNotificationSchemaMapUpdateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "historyNotificationSchemaMapUpdateV1", summary = "Update batch history notification schema map")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The history notification schema map batch was updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = HistoryNotificationSchemaMapListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/history_notification_schema_map/v1")
    public ResponseEntity<?> historyNotificationSchemaMapUpdateV1(
            @MapperContextBinding(roots = HistoryNotificationSchemaMapDTOMapperV1.class, response = HistoryNotificationSchemaMapListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody HistoryNotificationSchemaMapUpdateRqDTOv1 request) {
        HistoryNotificationSchemaMapListRsDTOv1 rs = new HistoryNotificationSchemaMapListRsDTOv1();
        try {
            List<HistoryNotificationSchemaMapUpdate> updateList = historyNotificationSchemaMapUpdateDTOReverseMapper.convertCollection(request.getHistoryNotificationSchemaMaps());
            List<HistoryNotificationSchemaMapEntity> historyNotificationSchemaMapList = historyNotificationSchemaMapService.updateHistoryNotificationSchemaMap(updateList);
            rs
                    .setHistoryNotificationSchemaMaps(historyNotificationSchemaMapListRsDTOMapper.convertCollection(historyNotificationSchemaMapList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
