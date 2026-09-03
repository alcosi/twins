package org.twins.core.controller.rest.priv.twinclass;

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
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;
import org.twins.core.domain.twinclass.TwinClassFieldValidatorCreate;
import org.twins.core.dto.rest.twinclass.TwinClassFieldValidatorCreateRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldValidatorListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldValidatorCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldValidatorRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclassfield.TwinClassFieldValidatorService;

import java.util.Collection;
import java.util.List;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.TWIN_CLASS_FIELD_CREATE)
public class TwinClassFieldValidatorCreateController extends ApiController {
    private final TwinClassFieldValidatorService twinClassFieldValidatorService;
    private final TwinClassFieldValidatorCreateRestDTOReverseMapper twinClassFieldValidatorCreateRestDTOReverseMapper;
    private final TwinClassFieldValidatorRestDTOMapper twinClassFieldValidatorRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldValidatorCreateV1", summary = "Twin class field validator batch create")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class field validator batch create", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldValidatorListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_field_validator/v1")
    public ResponseEntity<?> twinClassFieldValidatorCreateV1(
            @MapperContextBinding(roots = TwinClassFieldValidatorRestDTOMapper.class, response = TwinClassFieldValidatorListRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinClassFieldValidatorCreateRqDTOv1 request) {
        TwinClassFieldValidatorListRsDTOv1 rs = new TwinClassFieldValidatorListRsDTOv1();
        try {
            List<TwinClassFieldValidatorCreate> createList = twinClassFieldValidatorCreateRestDTOReverseMapper.convertCollection(request.getValidators());
            Collection<TwinClassFieldValidatorEntity> entities = twinClassFieldValidatorService.createTwinClassFieldValidators(createList);
            rs
                    .setValidators(twinClassFieldValidatorRestDTOMapper.convertCollection(entities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
