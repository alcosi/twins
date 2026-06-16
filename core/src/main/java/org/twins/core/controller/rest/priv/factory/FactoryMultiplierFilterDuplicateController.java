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
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterDuplicateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterListRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryMultiplierFilterDuplicateRestDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryMultiplierFilterRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryMultiplierFilterDuplicateService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_MULTIPLIER_CREATE})
public class FactoryMultiplierFilterDuplicateController extends ApiController {
    private final FactoryMultiplierFilterDuplicateService factoryMultiplierFilterDuplicateService;
    private final FactoryMultiplierFilterRestDTOMapper factoryMultiplierFilterRestDTOMapper;
    private final FactoryMultiplierFilterDuplicateRestDTOReverseMapper factoryMultiplierFilterDuplicateRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryMultiplierFilterDuplicateV1", summary = "Duplicates factory multiplier filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory multiplier filters copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryMultiplierFilterListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_multiplier_filter/duplicate/v1")
    public ResponseEntity<?> factoryMultiplierFilterDuplicateV1(
            @MapperContextBinding(roots = FactoryMultiplierFilterRestDTOMapper.class, response = FactoryMultiplierFilterListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Valid @RequestBody FactoryMultiplierFilterDuplicateRqDTOv1 request) {
        var rs = new FactoryMultiplierFilterListRsDTOv1();

        try {
            var duplicates = factoryMultiplierFilterDuplicateRestDTOReverseMapper.convertCollection(request.duplicates, mapperContext);
            var duplicatedFilters = factoryMultiplierFilterDuplicateService.duplicate(duplicates);
            rs
                    .setMultiplierFilters(factoryMultiplierFilterRestDTOMapper.convertCollection(duplicatedFilters, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
