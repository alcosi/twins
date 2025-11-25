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
import org.springframework.http.MediaType;
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
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dto.rest.factory.FactoryConditionSetListRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryConditionSetUpdateRqDTOv1;
import org.twins.core.mappers.rest.factory.FactoryConditionSetRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryConditionSetUpdateRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryConditionSetService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.CONDITION_SET_MANAGE, Permissions.CONDITION_SET_UPDATE})
public class FactoryConditionSetUpdateController extends ApiController {

    private final FactoryConditionSetService factoryConditionSetService;
    private final FactoryConditionSetUpdateRestDTOReverseMapper factoryConditionSetUpdateRestDTOReverseMapper;
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryConditionSetUpdateV1", summary = "Update condition set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update condition set", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
                    @Schema(implementation = FactoryConditionSetListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(
            value = "/private/factory_condition_set/v1",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> factoryConditionSetUpdateV1(
            @MapperContextBinding(roots = FactoryConditionSetRestDTOMapper.class, response = FactoryConditionSetListRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody FactoryConditionSetUpdateRqDTOv1 request) {
        FactoryConditionSetListRsDTOv1 rs = new FactoryConditionSetListRsDTOv1();
        try {
            List<TwinFactoryConditionSetEntity> conditionSet = factoryConditionSetService.updateFactoryConditionSet(
                    factoryConditionSetUpdateRestDTOReverseMapper.convertCollection(
                            request.getConditionSets(), mapperContext));
            rs
                    .setConditionSets(factoryConditionSetRestDTOMapper.convertCollection(conditionSet, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
