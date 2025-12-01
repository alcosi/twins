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
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.dto.rest.factory.FactoryConditionListRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryConditionUpdateRqDTOv1;
import org.twins.core.mappers.rest.factory.FactoryConditionRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryConditionUpdateRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryConditionService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.CONDITION_SET_MANAGE, Permissions.CONDITION_SET_UPDATE})
public class FactoryConditionUpdateController extends ApiController {

    private final FactoryConditionService factoryConditionService;
    private final FactoryConditionUpdateRestDTOReverseMapper factoryConditionUpdateRestDTOReverseMapper;
    private final FactoryConditionRestDTOMapper factoryConditionRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryConditionUpdateV1", summary = "Batch conditions update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update conditions", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
                    @Schema(implementation = FactoryConditionListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(
            value = "/private/factory_condition/v1",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> factoryConditionUpdateV1(
            @MapperContextBinding(roots = FactoryConditionRestDTOMapper.class, response = FactoryConditionListRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody FactoryConditionUpdateRqDTOv1 request) {

        FactoryConditionListRsDTOv1 rs = new FactoryConditionListRsDTOv1();
        try {
            List<TwinFactoryConditionEntity> conditions = factoryConditionService.updateFactoryConditions(
                    factoryConditionUpdateRestDTOReverseMapper.convertCollection(
                            request.getConditions(), mapperContext));
            rs
                    .setConditions(factoryConditionRestDTOMapper.convertCollection(conditions, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);

    }
}
