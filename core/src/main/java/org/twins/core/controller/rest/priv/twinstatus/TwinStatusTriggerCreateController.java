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
import org.twins.core.dao.twin.TwinStatusTriggerEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusTriggerCreateRqDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusTriggerCreateRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinstatus.TwinStatusTriggerCreateDTOReverseMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusTriggerRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinStatusTriggerService;

import java.util.List;

@Tag(name = ApiTag.TWIN_STATUS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_TRIGGER_MANAGE, Permissions.TWIN_TRIGGER_CREATE})
public class TwinStatusTriggerCreateController extends ApiController {
    private final TwinStatusTriggerService twinStatusTriggerService;
    private final TwinStatusTriggerRestDTOMapper twinStatusTriggerRestDTOMapper;
    private final TwinStatusTriggerCreateDTOReverseMapper twinStatusTriggerCreateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusTriggerCreateV1", summary = "Create twin status trigger")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin status trigger created successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusTriggerCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_status/trigger/v1")
    public ResponseEntity<?> twinStatusTriggerCreateV1(
            @MapperContextBinding(roots = TwinStatusTriggerRestDTOMapper.class, response = TwinStatusTriggerCreateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinStatusTriggerCreateRqDTOv1 request) {
        TwinStatusTriggerCreateRsDTOv1 rs = new TwinStatusTriggerCreateRsDTOv1();
        try {
            List<TwinStatusTriggerEntity> statusTriggerEntities = twinStatusTriggerCreateDTOReverseMapper.convertCollection(request.getTwinStatusTriggers());
            statusTriggerEntities = twinStatusTriggerService.createStatusTriggers(statusTriggerEntities);
            rs
                    .setTwinStatusTriggers(twinStatusTriggerRestDTOMapper.convertCollection(statusTriggerEntities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
