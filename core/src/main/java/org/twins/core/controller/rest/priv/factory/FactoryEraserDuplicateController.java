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
import org.twins.core.dto.rest.factory.FactoryEraserDuplicateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryEraserListRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryEraserRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryEraserDuplicateRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryEraserService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_ERASER_CREATE})
public class FactoryEraserDuplicateController extends ApiController {
    private final FactoryEraserService factoryEraserService;
    private final FactoryEraserRestDTOMapper factoryEraserRestDTOMapper;
    private final FactoryEraserDuplicateRestDTOReverseMapper factoryEraserDuplicateRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryEraserDuplicateV1", summary = "Duplicates factory erasers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory erasers copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryEraserListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_eraser/duplicate/v1")
    public ResponseEntity<?> factoryEraserDuplicateV1(
            @MapperContextBinding(roots = FactoryEraserRestDTOMapper.class, response = FactoryEraserListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody FactoryEraserDuplicateRqDTOv1 request) {
        var rs = new FactoryEraserListRsDTOv1();

        try {
            var duplicates = factoryEraserDuplicateRestDTOReverseMapper.convertCollection(request.duplicates, mapperContext);
            var duplicatedErasers = factoryEraserService.duplicateErasers(duplicates);
            rs
                    .setFactoryEraserList(factoryEraserRestDTOMapper.convertCollection(duplicatedErasers, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
