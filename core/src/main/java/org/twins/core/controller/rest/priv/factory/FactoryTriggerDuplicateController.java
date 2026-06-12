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
import org.twins.core.dto.rest.factory.FactoryTriggerDuplicateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryTriggerListRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryTriggerDuplicateRestDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryTriggerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryTriggerDuplicateService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_TRIGGER_CREATE})
public class FactoryTriggerDuplicateController extends ApiController {
    private final FactoryTriggerDuplicateService factoryTriggerDuplicateService;
    private final FactoryTriggerRestDTOMapper factoryTriggerRestDTOMapper;
    private final FactoryTriggerDuplicateRestDTOReverseMapper factoryTriggerDuplicateRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryTriggerDuplicateV1", summary = "Duplicates factory triggers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory triggers copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryTriggerListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_trigger/duplicate/v1")
    public ResponseEntity<?> factoryTriggerDuplicateV1(
            @MapperContextBinding(roots = FactoryTriggerRestDTOMapper.class, response = FactoryTriggerListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Valid @RequestBody FactoryTriggerDuplicateRqDTOv1 request) {
        var rs = new FactoryTriggerListRsDTOv1();

        try {
            var duplicates = factoryTriggerDuplicateRestDTOReverseMapper.convertCollection(request.duplicates, mapperContext);
            var duplicatedTriggers = factoryTriggerDuplicateService.duplicate(duplicates);
            rs
                    .setFactoryTriggerList(factoryTriggerRestDTOMapper.convertCollection(duplicatedTriggers, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
