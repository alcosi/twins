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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryEraserSaveRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryEraserUpdateRqDTOv1;
import org.twins.core.mappers.rest.factory.FactoryEraserRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryEraserUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryEraserService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "", name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.ERASER_MANAGE, Permissions.ERASER_UPDATE})
public class FactoryEraserUpdateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final FactoryEraserUpdateDTOReverseMapper factoryEraserUpdateDTOReverseMapper;
    private final FactoryEraserService factoryEraserService;
    private final FactoryEraserRestDTOMapper factoryEraserRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryEraserUpdateV1", summary = "Update factory eraser")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory eraser was updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryEraserSaveRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/factory/factory_eraser/{factoryEraserId}/v1")
    public ResponseEntity<?> factoryEraserUpdateV1(
            @MapperContextBinding(roots = FactoryEraserRestDTOMapper.class, response = FactoryEraserSaveRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACTORY_ERASER_ID) @PathVariable UUID factoryEraserId,
            @RequestBody FactoryEraserUpdateRqDTOv1 request) {
        FactoryEraserSaveRsDTOv1 rs = new FactoryEraserSaveRsDTOv1();
        try {
            TwinFactoryEraserEntity entity = factoryEraserUpdateDTOReverseMapper.convert(request.getEraser());
            entity = factoryEraserService.updateEraser(entity.setId(factoryEraserId));
            rs
                    .setEraser(factoryEraserRestDTOMapper.convert(entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
