package org.twins.core.controller.rest.priv.twinflow;

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
import org.twins.core.dto.rest.twinflow.TwinFactoryTriggerUpdateRqDTOv1;
import org.twins.core.dto.rest.twinflow.TwinFactoryTriggerUpdateRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TwinFactoryTriggerRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinFactoryTriggerUpdateDTOReverseMapper;
import org.twins.core.service.factory.FactoryTriggerService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_TRIGGER_MANAGE, Permissions.TWIN_TRIGGER_UPDATE})
public class TwinFactoryTriggerUpdateController extends ApiController {
    private final FactoryTriggerService factoryTriggerService;
    private final TwinFactoryTriggerRestDTOMapper twinFactoryTriggerRestDTOMapper;
    private final TwinFactoryTriggerUpdateDTOReverseMapper twinFactoryTriggerUpdateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinFactoryTriggerUpdateV1", summary = "Update twin factory triggers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin factory triggers updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinFactoryTriggerUpdateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin_factory/trigger/v1")
    public ResponseEntity<?> twinFactoryTriggerUpdateV1(
            @MapperContextBinding(roots = TwinFactoryTriggerRestDTOMapper.class, response = TwinFactoryTriggerUpdateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinFactoryTriggerUpdateRqDTOv1 request) {
        TwinFactoryTriggerUpdateRsDTOv1 rs = new TwinFactoryTriggerUpdateRsDTOv1();
        try {
            List<org.twins.core.dao.factory.TwinFactoryTriggerEntity> factoryTriggerEntities = twinFactoryTriggerUpdateDTOReverseMapper.convertCollection(request.getTwinFactoryTriggers());
            factoryTriggerEntities = factoryTriggerService.updateFactoryTriggers(factoryTriggerEntities);
            rs
                    .setTwinFactoryTriggers(twinFactoryTriggerRestDTOMapper.convertCollection(factoryTriggerEntities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
