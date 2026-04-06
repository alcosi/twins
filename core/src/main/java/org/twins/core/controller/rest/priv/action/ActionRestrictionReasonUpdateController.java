package org.twins.core.controller.rest.priv.action;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.domain.action.ActionRestrictionReasonUpdate;
import org.twins.core.dto.rest.action.ActionRestrictionReasonListRsDTOv1;
import org.twins.core.dto.rest.action.ActionRestrictionReasonUpdateRqDTOv1;
import org.twins.core.mappers.rest.action.ActionRestrictionReasonRestDTOMapper;
import org.twins.core.mappers.rest.action.ActionRestrictionReasonUpdateRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.action.ActionRestrictionReasonService;
import org.twins.core.service.permission.Permissions;

import java.util.Collection;
import java.util.List;

@Tag(name = ApiTag.ACTION_RESTRICTION)
@RestController
@RequiredArgsConstructor
@ProtectedBy(Permissions.ACTION_RESTRICTION_REASON_UPDATE)
public class ActionRestrictionReasonUpdateController extends ApiController {
    private final ActionRestrictionReasonService actionRestrictionReasonService;
    private final ActionRestrictionReasonUpdateRestDTOReverseMapper actionRestrictionReasonUpdateRestDTOReverseMapper;
    private final ActionRestrictionReasonRestDTOMapper actionRestrictionReasonRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "actionRestrictionReasonUpdateV1", summary = "Action restriction reason batch update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Action restriction reason batch update", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ActionRestrictionReasonListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/action_restriction_reason/v1")
    public ResponseEntity<?> actionRestrictionReasonUpdateV1(
            @MapperContextBinding(roots = ActionRestrictionReasonRestDTOMapper.class, response = ActionRestrictionReasonListRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody ActionRestrictionReasonUpdateRqDTOv1 request) {
        ActionRestrictionReasonListRsDTOv1 rs = new ActionRestrictionReasonListRsDTOv1();
        try {
            List<ActionRestrictionReasonUpdate> updateList = actionRestrictionReasonUpdateRestDTOReverseMapper.convertCollection(request.getActionRestrictionReasons());
            Collection<ActionRestrictionReasonEntity> actionRestrictionReasonEntityList = actionRestrictionReasonService.updateActionRestrictionReason(updateList);
            rs
                    .setActionRestrictionReasons(actionRestrictionReasonRestDTOMapper.convertCollection(actionRestrictionReasonEntityList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
