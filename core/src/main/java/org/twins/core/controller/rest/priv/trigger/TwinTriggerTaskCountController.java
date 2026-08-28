package org.twins.core.controller.rest.priv.trigger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
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
import org.twins.core.dto.rest.trigger.TwinTriggerTaskCountRqDTOv1;
import org.twins.core.dto.rest.trigger.TwinTriggerTaskCountRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.trigger.TwinTriggerTaskCountRestDTOMapper;
import org.twins.core.mappers.rest.trigger.TwinTriggerTaskSearchDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twintrigger.TwinTriggerTaskSearchService;

@Tag(description = "", name = ApiTag.TRIGGER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_TRIGGER_MANAGE, Permissions.TWIN_TRIGGER_VIEW})
public class TwinTriggerTaskCountController extends ApiController {
    private final TwinTriggerTaskSearchService twinTriggerTaskSearchService;
    private final TwinTriggerTaskSearchDTOReverseMapper twinTriggerTaskSearchDTOReverseMapper;
    private final TwinTriggerTaskCountRestDTOMapper twinTriggerTaskCountRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinTriggerTaskCountV1", summary = "Return count of twin trigger tasks grouped by specified fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTriggerTaskCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_trigger_task/count/v1")
    public ResponseEntity<?> twinTriggerTaskCountV1(
            @MapperContextBinding(roots = TwinTriggerTaskCountRestDTOMapper.class, response = TwinTriggerTaskCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid TwinTriggerTaskCountRqDTOv1 request) {
        TwinTriggerTaskCountRsDTOv1 rs = new TwinTriggerTaskCountRsDTOv1();
        try {
            var results = twinTriggerTaskSearchService
                    .countByGroupFields(twinTriggerTaskSearchDTOReverseMapper.convert(request.getSearch(), mapperContext), request.getGroupFields(), pagination);
            rs
                    .setCounts(twinTriggerTaskCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(results))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
