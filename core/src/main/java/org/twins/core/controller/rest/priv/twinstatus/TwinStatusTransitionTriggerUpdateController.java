package org.twins.core.controller.rest.priv.twinstatus;

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
import org.twins.core.dto.rest.twinstatus.TwinStatusTransitionTriggerUpdateRqDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusTransitionTriggerUpdateRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinstatus.TwinStatusTransitionTriggerRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusTransitionTriggerUpdateDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinStatusTransitionTriggerService;

import java.util.List;

@Tag(name = ApiTag.TWIN_STATUS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_TRIGGER_MANAGE, Permissions.TWIN_TRIGGER_UPDATE})
public class TwinStatusTransitionTriggerUpdateController extends ApiController {
    private final TwinStatusTransitionTriggerService twinStatusTransitionTriggerService;
    private final TwinStatusTransitionTriggerRestDTOMapper twinStatusTransitionTriggerRestDTOMapper;
    private final TwinStatusTransitionTriggerUpdateDTOReverseMapper twinStatusTransitionTriggerUpdateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusTransitionTriggerUpdateV1", summary = "Update twin status transition triggers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin status transition triggers updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusTransitionTriggerUpdateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin_status/trigger/v1")
    public ResponseEntity<?> twinStatusTransitionTriggerUpdateV1(
            @MapperContextBinding(roots = TwinStatusTransitionTriggerRestDTOMapper.class, response = TwinStatusTransitionTriggerUpdateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinStatusTransitionTriggerUpdateRqDTOv1 request) {
        TwinStatusTransitionTriggerUpdateRsDTOv1 rs = new TwinStatusTransitionTriggerUpdateRsDTOv1();
        try {
            List<org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity> statusTransitionTriggerEntities = twinStatusTransitionTriggerUpdateDTOReverseMapper.convertCollection(request.getTwinStatusTransitionTriggers());
            statusTransitionTriggerEntities = twinStatusTransitionTriggerService.updateStatusTransitionTriggers(statusTransitionTriggerEntities);
            rs
                    .setTwinStatusTransitionTriggers(twinStatusTransitionTriggerRestDTOMapper.convertCollection(statusTransitionTriggerEntities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
