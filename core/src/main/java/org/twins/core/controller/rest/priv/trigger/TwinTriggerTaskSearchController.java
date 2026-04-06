package org.twins.core.controller.rest.priv.trigger;

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
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dto.rest.trigger.TwinTriggerSearchRsDTOv1;
import org.twins.core.dto.rest.trigger.TwinTriggerTaskSearchRqDTOv1;
import org.twins.core.dto.rest.trigger.TwinTriggerTaskSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.trigger.TwinTriggerTaskRestDTOMapper;
import org.twins.core.mappers.rest.trigger.TwinTriggerTaskSearchDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.trigger.TwinTriggerTaskSearchService;

@Tag(description = "", name = ApiTag.TRIGGER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_TRIGGER_MANAGE, Permissions.TWIN_TRIGGER_VIEW})
public class TwinTriggerTaskSearchController extends ApiController {
    private final TwinTriggerTaskSearchService twinTriggerTaskSearchService;
    private final TwinTriggerTaskRestDTOMapper twinTriggerTaskRestDTOMapper;
    private final TwinTriggerTaskSearchDTOReverseMapper twinTriggerTaskSearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinTriggerTaskSearchV1", summary = "Search twin trigger tasks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTriggerTaskSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_trigger_task/search/v1")
    public ResponseEntity<?> twinTriggerTaskSearchV1(
            @MapperContextBinding(roots = TwinTriggerTaskRestDTOMapper.class, response = TwinTriggerTaskSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinTriggerTaskSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {
        TwinTriggerTaskSearchRsDTOv1 rs = new TwinTriggerTaskSearchRsDTOv1();
        try {
            PaginationResult<TwinTriggerTaskEntity> twinTriggerTaskList = twinTriggerTaskSearchService
                    .findTwinTriggerTasks(twinTriggerTaskSearchDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setPagination(paginationMapper.convert(twinTriggerTaskList))
                    .setTriggerTasks(twinTriggerTaskRestDTOMapper.convertCollection(twinTriggerTaskList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
