package org.twins.core.controller.rest.priv.validator;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.domain.validator.TwinValidatorCreate;
import org.twins.core.dto.rest.validator.TwinValidatorCreateRqDTOv1;
import org.twins.core.dto.rest.validator.TwinValidatorListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.validator.TwinValidatorCreateDTOReverseMapper;
import org.twins.core.mappers.rest.validator.TwinValidatorRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.validator.TwinValidatorService;

import java.util.List;

@Tag(description = "", name = ApiTag.TWIN_VALIDATOR)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_VALIDATOR_CREATE})
public class TwinValidatorCreateController extends ApiController {
    private final TwinValidatorService twinValidatorService;
    private final TwinValidatorRestDTOMapper twinValidatorRestDTOMapper;
    private final TwinValidatorCreateDTOReverseMapper twinValidatorCreateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinValidatorCreateV1", summary = "Create batch twin validators")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The twin validator batch was created successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinValidatorListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_validator/v1")
    public ResponseEntity<?> twinValidatorCreateV1(
            @MapperContextBinding(roots = TwinValidatorRestDTOMapper.class, response = TwinValidatorListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinValidatorCreateRqDTOv1 request) {
        TwinValidatorListRsDTOv1 rs = new TwinValidatorListRsDTOv1();
        try {
            List<TwinValidatorEntity> twinValidatorList = twinValidatorService.createTwinValidator(twinValidatorCreateDTOReverseMapper.convertCollection(request.getTwinValidators()));
            rs
                    .setTwinValidators(twinValidatorRestDTOMapper.convertCollection(twinValidatorList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
