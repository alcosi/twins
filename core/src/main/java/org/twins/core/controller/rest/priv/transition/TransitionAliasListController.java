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
import org.twins.core.dao.twinflow.TwinflowTransitionAliasEntity;
import org.twins.core.dto.rest.transition.TransitionAliasSearchRqDTOv1;
import org.twins.core.dto.rest.transition.TransitionAliasSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.twinflow.TransitionAliasRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TransitionAliasSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowTransitionService;

@Tag(name = ApiTag.TRANSITION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.TRANSITION_VIEW)
public class TransitionAliasListController extends ApiController {
    private final TransitionAliasSearchRestDTOReverseMapper transitionAliasSearchRestDTOReverseMapper;
    private final TransitionAliasRestDTOMapper transitionAliasRestDTOMapper;
    private final TwinflowTransitionService twinflowTransitionService;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "transitionAliasSearchV1", summary = "Transition alias search request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transition alias data list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TransitionAliasSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/transition_alias/search/v1")
    public ResponseEntity<?> transitionAliasSearchV1(
            @MapperContextBinding(roots = TransitionAliasRestDTOMapper.class, response = TransitionAliasSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TransitionAliasSearchRqDTOv1 request) {
        TransitionAliasSearchRsDTOv1 rs = new TransitionAliasSearchRsDTOv1();
        try {
            PaginationResult<TwinflowTransitionAliasEntity> transitionAliasList = twinflowTransitionService
                    .findTransitionAliases(transitionAliasSearchRestDTOReverseMapper.convert(request), pagination);
            rs
                    .setAliasList(transitionAliasRestDTOMapper.convertCollection(transitionAliasList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(transitionAliasList));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
