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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterViewRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryMultiplierFilterRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryMultiplierFilterService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Get factory multiplier filter", name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_MULTIPLIER_MANAGE, Permissions.FACTORY_MULTIPLIER_VIEW})
public class FactoryMultiplierFilterViewController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryMultiplierFilterRestDTOMapper factoryMultiplierFilterRestDTOMapper;
    private final FactoryMultiplierFilterService factoryMultiplierFilterService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryMultiplierFilterViewV1", summary = "Factory multiplier filter view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory multiplier filter data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryMultiplierFilterViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/factory_multiplier_filter/{multiplierId}/v1")
    public ResponseEntity<?> factoryMultiplierFilterViewV1(
            @MapperContextBinding(roots = FactoryMultiplierFilterRestDTOMapper.class, response = FactoryMultiplierFilterViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.MULTIPLIER_ID) @PathVariable("multiplierId") UUID multiplierId) {
        FactoryMultiplierFilterViewRsDTOv1 rs = new FactoryMultiplierFilterViewRsDTOv1();
        try {
            TwinFactoryMultiplierFilterEntity multiplierFilter = factoryMultiplierFilterService.findEntitySafe(multiplierId);

            rs
                    .setMultiplierFilter(factoryMultiplierFilterRestDTOMapper.convert(multiplierFilter, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
