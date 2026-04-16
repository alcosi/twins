package org.twins.core.controller.rest.priv.action;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dto.rest.action.ActionRestrictionReasonSearchRqDTOv1;
import org.twins.core.dto.rest.action.ActionRestrictionReasonSearchRsDTOv1;
import org.twins.core.mappers.rest.action.ActionRestrictionReasonRestDTOMapper;
import org.twins.core.mappers.rest.action.ActionRestrictionReasonSearchRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.action.ActionRestrictionReasonSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.ACTION_RESTRICTION)
@RestController
@RequiredArgsConstructor
@ProtectedBy({Permissions.ACTION_RESTRICTION_REASON_MANAGE, Permissions.ACTION_RESTRICTION_REASON_VIEW})
public class ActionRestrictionReasonSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final PaginationMapper paginationMapper;
    private final ActionRestrictionReasonSearchService actionRestrictionReasonSearchService;
    private final ActionRestrictionReasonSearchRestDTOReverseMapper actionRestrictionReasonSearchRestDTOReverseMapper;
    private final ActionRestrictionReasonRestDTOMapper actionRestrictionReasonRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "actionRestrictionReasonSearchV1", summary = "Action restriction reason search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Action restriction reason search", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ActionRestrictionReasonSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/action_restriction_reason/search/v1")
    public ResponseEntity<?> actionRestrictionReasonSearchV1(
            @MapperContextBinding(roots = ActionRestrictionReasonRestDTOMapper.class, response = ActionRestrictionReasonSearchRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody ActionRestrictionReasonSearchRqDTOv1 request) {
        ActionRestrictionReasonSearchRsDTOv1 rs = new ActionRestrictionReasonSearchRsDTOv1();
        try {
            PaginationResult<ActionRestrictionReasonEntity> reasonsList = actionRestrictionReasonSearchService.findActionRestrictionReasons(
                    actionRestrictionReasonSearchRestDTOReverseMapper.convert(request.search), pagination);
            rs
                    .setPagination(paginationMapper.convert(reasonsList))
                    .setActionRestrictionReasons(actionRestrictionReasonRestDTOMapper.convertCollection(reasonsList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
