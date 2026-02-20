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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dto.rest.trigger.TwinTriggerSearchRqDTOv1;
import org.twins.core.dto.rest.trigger.TwinTriggerSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.trigger.TwinTriggerRestDTOMapper;
import org.twins.core.mappers.rest.trigger.TwinTriggerSearchDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.trigger.TwinTriggerSearchService;

@Tag(description = "", name = ApiTag.TRIGGER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_TRIGGER_MANAGE, Permissions.TWIN_TRIGGER_VIEW})
public class TwinTriggerSearchController extends ApiController {
    private final TwinTriggerSearchService twinTriggerSearchService;
    private final TwinTriggerRestDTOMapper twinTriggerRestDTOMapper;
    private final TwinTriggerSearchDTOReverseMapper twinTriggerSearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinTriggerSearchV1", summary = "Search twin triggers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTriggerSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_trigger/search/v1")
    public ResponseEntity<?> twinTriggerSearchV1(
            @MapperContextBinding(roots = TwinTriggerRestDTOMapper.class, response = TwinTriggerSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinTriggerSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {
        TwinTriggerSearchRsDTOv1 rs = new TwinTriggerSearchRsDTOv1();
        try {
            PaginationResult<TwinTriggerEntity> twinTriggerList = twinTriggerSearchService
                    .findTwinTriggers(twinTriggerSearchDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setPagination(paginationMapper.convert(twinTriggerList))
                    .setTriggers(twinTriggerRestDTOMapper.convertCollection(twinTriggerList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
