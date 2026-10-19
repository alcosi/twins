package org.twins.core.controller.rest.priv.factory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dto.rest.factory.FactoryMultiplierListRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierUpdateRqDTOv1;
import org.twins.core.mappers.rest.factory.FactoryMultiplierRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryMultiplierUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryMultiplierService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(description = "", name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.FACTORY_MULTIPLIER_UPDATE)
public class FactoryMultiplierUpdateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final FactoryMultiplierUpdateDTOReverseMapper factoryMultiplierUpdateDTOReverseMapper;
    private final FactoryMultiplierService factoryMultiplierService;
    private final FactoryMultiplierRestDTOMapper factoryMultiplierRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryMultiplierUpdateV1", summary = "Update factory multiplier batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory multiplier batch was updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryMultiplierListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/factory_multiplier/v1")
    public ResponseEntity<?> factoryMultiplierUpdateV1(
            @MapperContextBinding(roots = FactoryMultiplierRestDTOMapper.class, response = FactoryMultiplierListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody @Valid FactoryMultiplierUpdateRqDTOv1 request) {
        FactoryMultiplierListRsDTOv1 rs = new FactoryMultiplierListRsDTOv1();
        try {
            List<TwinFactoryMultiplierEntity> entities = factoryMultiplierService.updateFactoryMultipliers(
                    factoryMultiplierUpdateDTOReverseMapper.convertCollection(request.getFactoryMultipliers()));
            rs
                    .setFactoryMultiplierList(factoryMultiplierRestDTOMapper.convertCollection(entities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
