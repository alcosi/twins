package org.twins.core.controller.rest.priv.scheduler;

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
import org.twins.core.dao.scheduler.SchedulerLogEntity;
import org.twins.core.dto.rest.scheduler.SchedulerLogSearchRqDTOv1;
import org.twins.core.dto.rest.scheduler.SchedulerLogSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.scheduler.SchedulerLogRestDTOMapperV1;
import org.twins.core.mappers.rest.scheduler.SchedulerLogSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.scheduler.SchedulerLogSearchService;

@Tag(name = ApiTag.SCHEDULER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.SCHEDULER_MANAGE, Permissions.SCHEDULER_VIEW})
public class SchedulerLogSearchController extends ApiController {

    private final SchedulerLogRestDTOMapperV1 schedulerLogRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final SchedulerLogSearchService schedulerLogSearchService;
    private final SchedulerLogSearchRestDTOReverseMapper schedulerLogSearchRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "schedulerLogSearchV1", summary = "Returns scheduler log search result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scheduler log list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = SchedulerLogSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/scheduler_log/search/v1")
    public ResponseEntity<?> schedulerLogSearchV1(
            @MapperContextBinding(roots = SchedulerLogRestDTOMapperV1.class, response = SchedulerLogSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody SchedulerLogSearchRqDTOv1 request
    ) {
        SchedulerLogSearchRsDTOv1 rs = new SchedulerLogSearchRsDTOv1();

        try {
            PaginationResult<SchedulerLogEntity> schedulerLogList = schedulerLogSearchService.search(schedulerLogSearchRestDTOReverseMapper.convert(request.getSearch()), pagination);

            rs
                    .setPagination(paginationMapper.convert(schedulerLogList))
                    .setSchedulerLogs(schedulerLogRestDTOMapper.convertCollection(schedulerLogList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
