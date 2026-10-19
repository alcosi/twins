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
import org.twins.core.dto.rest.factory.FactoryConditionSetDuplicateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryConditionSetListRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryConditionSetDuplicateRestDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryConditionSetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryConditionSetDuplicateService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_CONDITION_SET_CREATE})
public class FactoryConditionSetDuplicateController extends ApiController {
    private final FactoryConditionSetDuplicateService factoryConditionSetDuplicateService;
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;
    private final FactoryConditionSetDuplicateRestDTOReverseMapper factoryConditionSetDuplicateRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryConditionSetDuplicateV1", summary = "Duplicates factory condition sets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory condition sets copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryConditionSetListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_condition_set/duplicate/v1")
    public ResponseEntity<?> factoryConditionSetDuplicateV1(
            @MapperContextBinding(roots = FactoryConditionSetRestDTOMapper.class, response = FactoryConditionSetListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Valid @RequestBody FactoryConditionSetDuplicateRqDTOv1 request) {
        var rs = new FactoryConditionSetListRsDTOv1();

        try {
            var duplicates = factoryConditionSetDuplicateRestDTOReverseMapper.convertCollection(request.duplicates, mapperContext);
            var duplicatedConditionSets = factoryConditionSetDuplicateService.duplicate(duplicates);
            rs
                    .setConditionSets(factoryConditionSetRestDTOMapper.convertCollection(duplicatedConditionSets, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
