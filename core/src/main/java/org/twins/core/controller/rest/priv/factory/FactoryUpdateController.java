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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dto.rest.factory.FactoryListRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryUpdateDTOv1;
import org.twins.core.dto.rest.factory.FactoryUpdateRqDTOv1;
import org.twins.core.mappers.rest.factory.FactoryRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryService;
import org.twins.core.service.permission.Permissions;

import java.util.ArrayList;
import java.util.List;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.FACTORY_UPDATE)
public class FactoryUpdateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final FactoryUpdateDTOReverseMapper factoryUpdateDTOReverseMapper;
    private final FactoryRestDTOMapper factoryRestDTOMapper;
    private final FactoryService factoryService;
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryUpdateV1", summary = "Factory batch update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory batch update", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/factory/v1")
    public ResponseEntity<?> factoryUpdateV1(
            @MapperContextBinding(roots = FactoryRestDTOMapper.class, response = FactoryListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody @Valid FactoryUpdateRqDTOv1 request) {
        FactoryListRsDTOv1 rs = new FactoryListRsDTOv1();
        try {
            List<I18nEntity> namesI18n = new ArrayList<>(request.getFactories().size());
            List<I18nEntity> descriptionsI18n = new ArrayList<>(request.getFactories().size());
            for (FactoryUpdateDTOv1 factory : request.getFactories()) {
                namesI18n.add(i18NSaveRestDTOReverseMapper.convert(factory.getNameI18n()));
                descriptionsI18n.add(i18NSaveRestDTOReverseMapper.convert(factory.getDescriptionI18n()));
            }
            List<TwinFactoryEntity> factoryEntities = factoryService.updateFactories(
                    factoryUpdateDTOReverseMapper.convertCollection(request.getFactories()), namesI18n, descriptionsI18n);
            rs
                    .setFactoryList(factoryRestDTOMapper.convertCollection(factoryEntities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
