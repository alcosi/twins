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
import org.twins.core.dto.rest.twinstatus.TwinStatusTriggerSearchRqDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusTriggerSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinstatus.TwinStatusTriggerRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusTriggerSearchDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinStatusTriggerSearchService;

@Tag(name = ApiTag.TWIN_STATUS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_TRIGGER_MANAGE, Permissions.TWIN_TRIGGER_VIEW})
public class TwinStatusTriggerSearchController extends ApiController {
    private final TwinStatusTriggerSearchService twinStatusTriggerSearchService;
    private final TwinStatusTriggerRestDTOMapper twinStatusTriggerRestDTOMapper;
    private final TwinStatusTriggerSearchDTOReverseMapper twinStatusTriggerSearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusTriggerSearchV1", summary = "Search twin status triggers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusTriggerSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_status/trigger/search/v1")
    public ResponseEntity<?> twinStatusTriggerSearchV1(
            @MapperContextBinding(roots = TwinStatusTriggerRestDTOMapper.class, response = TwinStatusTriggerSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinStatusTriggerSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {
        TwinStatusTriggerSearchRsDTOv1 rs = new TwinStatusTriggerSearchRsDTOv1();
        try {
            PaginationResult<org.twins.core.dao.twin.TwinStatusTriggerEntity> statusTriggerList = twinStatusTriggerSearchService
                    .findStatusTriggers(twinStatusTriggerSearchDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setPagination(paginationMapper.convert(statusTriggerList))
                    .setTwinStatusTriggers(twinStatusTriggerRestDTOMapper.convertCollection(statusTriggerList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
