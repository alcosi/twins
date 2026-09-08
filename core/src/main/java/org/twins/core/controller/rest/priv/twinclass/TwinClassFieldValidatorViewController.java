package org.twins.core.controller.rest.priv.twinclass;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TwinClassFieldValidatorViewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldValidatorRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclassfield.TwinClassFieldValidatorService;

import java.util.UUID;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_FIELD_MANAGE, Permissions.TWIN_CLASS_FIELD_VIEW})
public class TwinClassFieldValidatorViewController extends ApiController {
    private final TwinClassFieldValidatorService twinClassFieldValidatorService;
    private final TwinClassFieldValidatorRestDTOMapper twinClassFieldValidatorRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldValidatorViewV1", summary = "Twin class field validator view by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class field validator details", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldValidatorViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin_class_field_validator/{twinClassFieldValidatorId}/v1")
    public ResponseEntity<?> twinClassFieldValidatorViewV1(
            @MapperContextBinding(roots = TwinClassFieldValidatorRestDTOMapper.class, response = TwinClassFieldValidatorViewRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.UUID_ID) @PathVariable UUID twinClassFieldValidatorId) {
        TwinClassFieldValidatorViewRsDTOv1 rs = new TwinClassFieldValidatorViewRsDTOv1();
        try {
            TwinClassFieldValidatorEntity entity = twinClassFieldValidatorService.findEntitySafe(twinClassFieldValidatorId);
            rs
                    .setValidator(twinClassFieldValidatorRestDTOMapper.convert(entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
