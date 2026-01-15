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
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dto.rest.scheduler.SchedulerSearchRqDTOv1;
import org.twins.core.dto.rest.scheduler.SchedulerSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.scheduler.SchedulerRestDTOMapperV1;
import org.twins.core.mappers.rest.scheduler.SchedulerSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.scheduler.SchedulerSearchService;

@Tag(name = ApiTag.SCHEDULER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.SCHEDULER_MANAGE, Permissions.SCHEDULER_VIEW})
public class SchedulerSearchController extends ApiController {

    private final SchedulerRestDTOMapperV1 schedulerRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final SchedulerSearchService schedulerSearchService;
    private final SchedulerSearchRestDTOReverseMapper schedulerSearchRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "schedulerSearchV1", summary = "Returns scheduler search result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scheduler list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = SchedulerSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/scheduler/search/v1")
    public ResponseEntity<?> schedulerSearchV1(
            @MapperContextBinding(roots = SchedulerRestDTOMapperV1.class, response = SchedulerSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody SchedulerSearchRqDTOv1 request
    ) {
        SchedulerSearchRsDTOv1 rs = new SchedulerSearchRsDTOv1();

        try {
            PaginationResult<SchedulerEntity> schedulerList = schedulerSearchService.search(schedulerSearchRestDTOReverseMapper.convert(request.getSearch()), pagination);

            rs
                    .setPagination(paginationMapper.convert(schedulerList))
                    .setSchedulers(schedulerRestDTOMapper.convertCollection(schedulerList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
