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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.dto.rest.notification.NotificationSchemaDTOv1;
import org.twins.core.dto.rest.notification.NotificationSchemaSearchRqDTOv1;
import org.twins.core.dto.rest.notification.NotificationSchemaSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.notification.NotificationSchemaRestDTOMapper;
import org.twins.core.mappers.rest.notification.NotificationSchemaSearchDTOReverseMapper;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.notification.NotificationSchemaSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "Search notification schema", name = ApiTag.NOTIFICATION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.NOTIFICATION_SCHEMA_MANAGE, Permissions.NOTIFICATION_SCHEMA_VIEW})
public class NotificationSchemaSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final PaginationMapper paginationMapper;
    private final NotificationSchemaSearchService notificationSchemaSearchService;
    private final NotificationSchemaRestDTOMapper notificationSchemaRestDTOMapper;
    private final NotificationSchemaSearchDTOReverseMapper notificationSchemaSearchDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "notificationSchemaSearchV1", summary = "Return a list of all notification schemas for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = NotificationSchemaSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/notification_schema/search/v1")
    public ResponseEntity<?> notificationSchemaSearchV1(
            @MapperContextBinding(roots = NotificationSchemaRestDTOMapper.class, response = NotificationSchemaSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody NotificationSchemaSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {
        NotificationSchemaSearchRsDTOv1 rs = new NotificationSchemaSearchRsDTOv1();
        try {
            PaginationResult<NotificationSchemaEntity> notificationSchemaList = notificationSchemaSearchService
                    .findNotificationSchemasByDomain(notificationSchemaSearchDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setPagination(paginationMapper.convert(notificationSchemaList))
                    .setNotificationSchemas(notificationSchemaRestDTOMapper.convertCollection(notificationSchemaList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
