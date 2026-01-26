package org.twins.core.controller.rest.priv.transition;

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
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dto.rest.transition.TransitionTriggerSearchRqDTOv1;
import org.twins.core.dto.rest.transition.TransitionTriggerSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TransitionTriggerRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TransitionTriggerSearchDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TransitionTriggerSearchService;

@Tag(name = ApiTag.TRANSITION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TRANSITION_MANAGE, Permissions.TRANSITION_VIEW})
public class TransitionTriggerSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final TransitionTriggerSearchDTOReverseMapper transitionTriggerSearchDTOReverseMapper;
    private final TransitionTriggerRestDTOMapper transitionTriggerRestDTOMapper;
    private final TransitionTriggerSearchService transitionTriggerSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "transitionTriggerSearchV1", summary = "Search data list of transition triggers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of transition triggers", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TransitionTriggerSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/transition_trigger/search/v1")
    public ResponseEntity<?> transitionTriggerSearchV1(
            @MapperContextBinding(roots = TransitionTriggerRestDTOMapper.class, response = TransitionTriggerSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TransitionTriggerSearchRqDTOv1 request) {
        TransitionTriggerSearchRsDTOv1 rs = new TransitionTriggerSearchRsDTOv1();
        try {
            PaginationResult<TwinflowTransitionTriggerEntity> triggerList = transitionTriggerSearchService
                    .findTransitionTriggers(transitionTriggerSearchDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setTriggers(transitionTriggerRestDTOMapper.convertCollection(triggerList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(triggerList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
