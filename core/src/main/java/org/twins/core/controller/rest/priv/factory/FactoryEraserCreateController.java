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
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dto.rest.factory.FactoryEraserCreateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryEraserListRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryEraserCreateDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryEraserRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryEraserService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(description = "", name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.FACTORY_ERASER_CREATE)
public class FactoryEraserCreateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final FactoryEraserCreateDTOReverseMapper factoryEraserCreateDTOReverseMapper;
    private final FactoryEraserRestDTOMapper factoryEraserRestDTOMapper;
    private final FactoryEraserService factoryEraserService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryEraserCreateV1", summary = "Create factory eraser batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory eraser batch created successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryEraserListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory/factory_eraser/v1")
    public ResponseEntity<?> factoryEraserCreateV1(
            @MapperContextBinding(roots = FactoryEraserRestDTOMapper.class, response = FactoryEraserListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody @Valid FactoryEraserCreateRqDTOv1 request) {
        FactoryEraserListRsDTOv1 rs = new FactoryEraserListRsDTOv1();
        try {
            List<TwinFactoryEraserEntity> entities = factoryEraserService.createErasers(
                    factoryEraserCreateDTOReverseMapper.convertCollection(request.getErasers(), mapperContext));
            rs
                    .setFactoryEraserList(factoryEraserRestDTOMapper.convertCollection(entities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
