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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dto.rest.factory.FactoryMultiplierCreateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierListRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryMultiplierCreateDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryMultiplierRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryMultiplierService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.FACTORY_MULTIPLIER_CREATE)
public class FactoryMultiplierCreateController extends ApiController {
    private final FactoryMultiplierRestDTOMapper factoryMultiplierRestDTOMapper;
    private final FactoryMultiplierCreateDTOReverseMapper factoryMultiplierCreateDTOReverseMapper;
    private final FactoryMultiplierService factoryMultiplierService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryMultiplierCreateV1", summary = "Create factory multiplier batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory multiplier batch created successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryMultiplierListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_multiplier/v1")
    public ResponseEntity<?> factoryMultiplierCreateV1(
            @MapperContextBinding(roots = FactoryMultiplierRestDTOMapper.class, response = FactoryMultiplierListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody @Valid FactoryMultiplierCreateRqDTOv1 request) {
        FactoryMultiplierListRsDTOv1 rs = new FactoryMultiplierListRsDTOv1();
        try {
            List<TwinFactoryMultiplierEntity> entities = factoryMultiplierService.createFactoryMultipliers(
                    factoryMultiplierCreateDTOReverseMapper.convertCollection(request.getFactoryMultipliers()));
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
