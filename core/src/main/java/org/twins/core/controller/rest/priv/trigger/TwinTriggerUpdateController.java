package org.twins.core.controller.rest.priv.trigger;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.domain.trigger.TwinTriggerUpdate;
import org.twins.core.dto.rest.trigger.TwinTriggerListRsDTOv1;
import org.twins.core.dto.rest.trigger.TwinTriggerUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.trigger.TwinTriggerRestDTOMapper;
import org.twins.core.mappers.rest.trigger.TwinTriggerUpdateDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.trigger.TwinTriggerService;

import java.util.List;

@Tag(description = "", name = ApiTag.TRIGGER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_TRIGGER_MANAGE, Permissions.TWIN_TRIGGER_UPDATE})
public class TwinTriggerUpdateController extends ApiController {
    private final TwinTriggerService twinTriggerService;
    private final TwinTriggerRestDTOMapper twinTriggerRestDTOMapper;
    private final TwinTriggerUpdateDTOReverseMapper twinTriggerUpdateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinTriggerUpdateV1", summary = "Update twin triggers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The twin triggers were updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTriggerListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin_trigger/v1")
    public ResponseEntity<?> twinTriggerUpdateV1(
            @MapperContextBinding(roots = TwinTriggerRestDTOMapper.class, response = TwinTriggerListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinTriggerUpdateRqDTOv1 request) {
        TwinTriggerListRsDTOv1 rs = new TwinTriggerListRsDTOv1();
        try {
            List<TwinTriggerUpdate> updateList = twinTriggerUpdateDTOReverseMapper.convertCollection(request.getTriggers());
            List<TwinTriggerEntity> twinTriggerList = twinTriggerService.updateTriggers(updateList);
            rs
                    .setTriggers(twinTriggerRestDTOMapper.convertCollection(twinTriggerList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
