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
import org.twins.core.dto.rest.factory.FactoryDuplicateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.factory.FactoryDuplicateRestDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.factory.TwinFactoryService;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_CREATE})
public class FactoryDuplicateController extends ApiController {
    private final TwinFactoryService twinFactoryService;
    private final FactoryDuplicateRestDTOReverseMapper factoryDuplicateRestDTOReverseMapper;
    private final FactoryRestDTOMapper factoryRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryDuplicateV1", summary = "Duplicates factories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factories copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory/duplicate/v1")
    public ResponseEntity<?> factoryDuplicateV1(
            @MapperContextBinding(roots = FactoryRestDTOMapper.class, response = FactoryListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody FactoryDuplicateRqDTOv1 request) {
        var rs = new FactoryListRsDTOv1();

        try {
            var duplicates = factoryDuplicateRestDTOReverseMapper.convertCollection(request.duplicates, mapperContext);
            var duplicatedFactories = twinFactoryService.duplicate(duplicates);
            rs
                    .setFactoryList(factoryRestDTOMapper.convertCollection(duplicatedFactories, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
