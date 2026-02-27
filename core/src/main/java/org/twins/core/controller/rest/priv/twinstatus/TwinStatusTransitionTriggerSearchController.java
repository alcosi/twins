package org.twins.core.controller.rest.priv.twinstatus;

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
import org.twins.core.dto.rest.twinstatus.TwinStatusTransitionTriggerSearchRqDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusTransitionTriggerSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinstatus.TwinStatusTransitionTriggerRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusTransitionTriggerSearchDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinStatusTransitionTriggerSearchService;

@Tag(name = ApiTag.TWIN_STATUS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_TRIGGER_MANAGE, Permissions.TWIN_TRIGGER_VIEW})
public class TwinStatusTransitionTriggerSearchController extends ApiController {
    private final TwinStatusTransitionTriggerSearchService twinStatusTransitionTriggerSearchService;
    private final TwinStatusTransitionTriggerRestDTOMapper twinStatusTransitionTriggerRestDTOMapper;
    private final TwinStatusTransitionTriggerSearchDTOReverseMapper twinStatusTransitionTriggerSearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusTransitionTriggerSearchV1", summary = "Search twin status transition triggers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusTransitionTriggerSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_status/trigger/search/v1")
    public ResponseEntity<?> twinStatusTransitionTriggerSearchV1(
            @MapperContextBinding(roots = TwinStatusTransitionTriggerRestDTOMapper.class, response = TwinStatusTransitionTriggerSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinStatusTransitionTriggerSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {
        TwinStatusTransitionTriggerSearchRsDTOv1 rs = new TwinStatusTransitionTriggerSearchRsDTOv1();
        try {
            PaginationResult<org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity> statusTransitionTriggerList = twinStatusTransitionTriggerSearchService
                    .findStatusTransitionTriggers(twinStatusTransitionTriggerSearchDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setPagination(paginationMapper.convert(statusTransitionTriggerList))
                    .setTwinStatusTransitionTriggers(twinStatusTransitionTriggerRestDTOMapper.convertCollection(statusTransitionTriggerList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
