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
import org.twins.core.dto.rest.factory.FactoryMultiplierDuplicateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierListRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryMultiplierDuplicateRestDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryMultiplierRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryMultiplierDuplicateService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_MULTIPLIER_CREATE})
public class FactoryMultiplierDuplicateController extends ApiController {
    private final FactoryMultiplierDuplicateService factoryMultiplierDuplicateService;
    private final FactoryMultiplierRestDTOMapper factoryMultiplierRestDTOMapper;
    private final FactoryMultiplierDuplicateRestDTOReverseMapper factoryMultiplierDuplicateRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryMultiplierDuplicateV1", summary = "Duplicates factory multipliers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory multipliers copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryMultiplierListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_multiplier/duplicate/v1")
    public ResponseEntity<?> factoryMultiplierDuplicateV1(
            @MapperContextBinding(roots = FactoryMultiplierRestDTOMapper.class, response = FactoryMultiplierListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Valid @RequestBody FactoryMultiplierDuplicateRqDTOv1 request) {
        var rs = new FactoryMultiplierListRsDTOv1();

        try {
            var duplicates = factoryMultiplierDuplicateRestDTOReverseMapper.convertCollection(request.duplicates, mapperContext);
            var duplicatedMultipliers = factoryMultiplierDuplicateService.duplicate(duplicates);
            rs
                    .setFactoryMultiplierList(factoryMultiplierRestDTOMapper.convertCollection(duplicatedMultipliers, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
