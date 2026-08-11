package org.twins.core.controller.rest.priv.factory;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dto.rest.factory.FactoryTriggerCreateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryTriggerListRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryTriggerCreateDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryTriggerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryTriggerService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_TRIGGER_MANAGE, Permissions.TWIN_TRIGGER_CREATE})
public class FactoryTriggerCreateController extends ApiController {
    private final FactoryTriggerService factoryTriggerService;
    private final FactoryTriggerRestDTOMapper factoryTriggerRestDTOMapper;
    private final FactoryTriggerCreateDTOReverseMapper factoryTriggerCreateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinFactoryTriggerCreateV1", summary = "Create twin factory trigger")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin factory trigger created successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryTriggerListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_factory/trigger/v1")
    public ResponseEntity<?> twinFactoryTriggerCreateV1(
            @MapperContextBinding(roots = FactoryTriggerRestDTOMapper.class, response = FactoryTriggerListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody FactoryTriggerCreateRqDTOv1 request) {
        FactoryTriggerListRsDTOv1 rs = new FactoryTriggerListRsDTOv1();
        try {
            List<TwinFactoryTriggerEntity> factoryTriggerEntities = factoryTriggerCreateDTOReverseMapper.convertCollection(request.getTwinFactoryTriggers());
            factoryTriggerEntities = factoryTriggerService.createFactoryTriggers(factoryTriggerEntities);
            rs
                    .setFactoryTriggerList(factoryTriggerRestDTOMapper.convertCollection(factoryTriggerEntities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
