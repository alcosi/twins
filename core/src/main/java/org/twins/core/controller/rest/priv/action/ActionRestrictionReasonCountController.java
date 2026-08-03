package org.twins.core.controller.rest.priv.action;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dto.rest.action.ActionRestrictionReasonCountRqDTOv1;
import org.twins.core.dto.rest.action.ActionRestrictionReasonCountRsDTOv1;
import org.twins.core.mappers.rest.action.ActionRestrictionReasonCountRestDTOMapper;
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
public class ActionRestrictionReasonCountController extends ApiController {
    private final ActionRestrictionReasonSearchService actionRestrictionReasonSearchService;
    private final ActionRestrictionReasonSearchRestDTOReverseMapper actionRestrictionReasonSearchRestDTOReverseMapper;
    private final ActionRestrictionReasonCountRestDTOMapper actionRestrictionReasonCountRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "actionRestrictionReasonCountV1", summary = "Return count of action restriction reasons grouped by specified fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ActionRestrictionReasonCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/action_restriction_reason/count/v1")
    public ResponseEntity<?> actionRestrictionReasonCountV1(
            @MapperContextBinding(roots = ActionRestrictionReasonCountRestDTOMapper.class, response = ActionRestrictionReasonCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid ActionRestrictionReasonCountRqDTOv1 request) {
        ActionRestrictionReasonCountRsDTOv1 rs = new ActionRestrictionReasonCountRsDTOv1();
        try {
            var results =
                    actionRestrictionReasonSearchService.countByGroupFields(actionRestrictionReasonSearchRestDTOReverseMapper
                            .convert(request.getSearch(), mapperContext), request.getGroupFields(), pagination);
            rs
                    .setCounts(actionRestrictionReasonCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
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
