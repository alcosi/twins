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
import org.twins.core.dto.rest.factory.FactoryConditionDuplicateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryConditionListRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryConditionDuplicateRestDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryConditionRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryConditionDuplicateService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_CONDITION_SET_CREATE})
public class FactoryConditionDuplicateController extends ApiController {
    private final FactoryConditionDuplicateService factoryConditionDuplicateService;
    private final FactoryConditionRestDTOMapper factoryConditionRestDTOMapper;
    private final FactoryConditionDuplicateRestDTOReverseMapper factoryConditionDuplicateRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryConditionDuplicateV1", summary = "Duplicates factory conditions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory conditions copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryConditionListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_condition/duplicate/v1")
    public ResponseEntity<?> factoryConditionDuplicateV1(
            @MapperContextBinding(roots = FactoryConditionRestDTOMapper.class, response = FactoryConditionListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Valid @RequestBody FactoryConditionDuplicateRqDTOv1 request) {
        var rs = new FactoryConditionListRsDTOv1();

        try {
            var duplicates = factoryConditionDuplicateRestDTOReverseMapper.convertCollection(request.duplicates, mapperContext);
            var duplicatedConditions = factoryConditionDuplicateService.duplicate(duplicates);
            rs
                    .setConditions(factoryConditionRestDTOMapper.convertCollection(duplicatedConditions, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
