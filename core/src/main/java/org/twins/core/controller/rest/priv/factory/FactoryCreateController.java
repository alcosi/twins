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
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dto.rest.factory.FactoryCreateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryCreateDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryRestDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.TwinFactoryService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_MANAGE, Permissions.FACTORY_CREATE})
public class FactoryCreateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final FactoryCreateDTOReverseMapper factoryCreateDTOReverseMapper;
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;
    private final FactoryRestDTOMapper factoryRestDTOMapper;
    private final TwinFactoryService twinFactoryService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryCreateV1", summary = "Factory add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory data add", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory/v1")
    public ResponseEntity<?> factoryCreateV1(
            @MapperContextBinding(roots = FactoryRestDTOMapper.class, response = FactoryRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody FactoryCreateRqDTOv1 request) {
        FactoryRsDTOv1 rs = new FactoryRsDTOv1();
        try {
            I18nEntity nameI18n = i18NSaveRestDTOReverseMapper.convert(request.getNameI18n(), mapperContext);
            I18nEntity descriptionI18n = i18NSaveRestDTOReverseMapper.convert(request.getDescriptionI18n(), mapperContext);
            TwinFactoryEntity factoryEntity = twinFactoryService.createFactory(factoryCreateDTOReverseMapper.convert(request), nameI18n, descriptionI18n);
            rs
                    .setFactory(factoryRestDTOMapper.convert(factoryEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
