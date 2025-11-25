package org.twins.core.controller.rest.priv.factory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryMultiplierRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierUpdateRqDTOv1;
import org.twins.core.mappers.rest.factory.FactoryMultiplierRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryMultiplierUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryMultiplierService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.MULTIPLIER_MANAGE, Permissions.MULTIPLIER_UPDATE})
public class FactoryMultiplierUpdateController extends ApiController {
    private final FactoryMultiplierRestDTOMapper factoryMultiplierRestDTOMapper;
    private final FactoryMultiplierUpdateDTOReverseMapper factoryMultiplierUpdateDTOReverseMapper;
    private final FactoryMultiplierService factoryMultiplierService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryMultiplierUpdateV1", summary = "Factory multiplier update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory data multiplier update", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryMultiplierRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/factory_multiplier/{factoryMultiplierId}/v1")
    public ResponseEntity<?> factoryMultiplierUpdateV1(
            @MapperContextBinding(roots = FactoryMultiplierRestDTOMapper.class, response = FactoryMultiplierRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACTORY_MULTIPLIER_ID) @PathVariable UUID factoryMultiplierId,
            @RequestBody FactoryMultiplierUpdateRqDTOv1 request) {
        FactoryMultiplierRsDTOv1 rs = new FactoryMultiplierRsDTOv1();
        try {
            TwinFactoryMultiplierEntity multiplierEntity = factoryMultiplierUpdateDTOReverseMapper.convert(request.getFactoryMultiplier())
                    .setId(factoryMultiplierId);
            multiplierEntity = factoryMultiplierService.updateFactoryMultiplier(multiplierEntity);
            rs
                    .setFactoryMultiplier(factoryMultiplierRestDTOMapper.convert(multiplierEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
