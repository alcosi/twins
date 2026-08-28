package org.twins.core.controller.rest.priv.validator;

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
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.domain.validator.TwinValidatorUpdate;
import org.twins.core.dto.rest.validator.TwinValidatorListRsDTOv1;
import org.twins.core.dto.rest.validator.TwinValidatorUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.validator.TwinValidatorRestDTOMapper;
import org.twins.core.mappers.rest.validator.TwinValidatorUpdateRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinvalidator.TwinValidatorService;

import java.util.Collection;
import java.util.List;

@Tag(name = ApiTag.TWIN_VALIDATOR)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.TWIN_VALIDATOR_UPDATE)
public class TwinValidatorUpdateController extends ApiController {
    private final TwinValidatorService twinValidatorService;
    private final TwinValidatorUpdateRestDTOReverseMapper twinValidatorUpdateRestDTOReverseMapper;
    private final TwinValidatorRestDTOMapper twinValidatorRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinValidatorUpdateV1", summary = "Twin validator batch update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin validator batch update", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinValidatorListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin_validator/v1")
    public ResponseEntity<?> twinValidatorUpdateV1(
            @MapperContextBinding(roots = TwinValidatorRestDTOMapper.class, response = TwinValidatorListRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody @Valid TwinValidatorUpdateRqDTOv1 request) {
        TwinValidatorListRsDTOv1 rs = new TwinValidatorListRsDTOv1();
        try {
            List<TwinValidatorUpdate> updateList = twinValidatorUpdateRestDTOReverseMapper.convertCollection(request.getValidators());
            Collection<TwinValidatorEntity> twinValidatorEntityList = twinValidatorService.updateTwinValidators(updateList);
            rs
                    .setValidators(twinValidatorRestDTOMapper.convertCollection(twinValidatorEntityList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
