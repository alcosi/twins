package org.twins.core.controller.rest.priv.twinflow;

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
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TransitionSearchRqDTOv1;
import org.twins.core.dto.rest.twinflow.TransitionSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV2RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TransitionSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowTransitionService;

@Tag(description = "", name = ApiTag.TRANSITION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.TRANSITION_VIEW)
public class TransitionListController extends ApiController {
    private final TransitionSearchRestDTOReverseMapper transitionSearchRestDTOReverseMapper;
    private final TransitionBaseV2RestDTOMapper transitionBaseV2RestDTOMapper;
    private final TwinflowTransitionService transitionService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "transitionSearchV1", summary = "Returns transition search result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transition data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TransitionSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/transition/search/v1")
    public ResponseEntity<?> transitionSearchV1(
            @MapperContextBinding(roots = TransitionBaseV2RestDTOMapper.class, response = TransitionSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TransitionSearchRqDTOv1 request) {
        TransitionSearchRsDTOv1 rs = new TransitionSearchRsDTOv1();
        try {
            PaginationResult<TwinflowTransitionEntity> transitions = transitionService.search(transitionSearchRestDTOReverseMapper.convert(request), pagination);
            rs
                    .setTransition(transitionBaseV2RestDTOMapper.convertCollection(transitions.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(transitions))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
