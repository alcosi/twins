package org.twins.core.controller.rest.priv.twin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.exception.TwinFieldValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.TwinChangeResponse;
import org.twins.core.dto.rest.twin.TwinCreateRqDTOv2;
import org.twins.core.mappers.rest.twin.TwinCreateRqRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinService;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_MANAGE, Permissions.TWIN_VIEW})
public class TwinCreatedValidateController extends ApiController {
    private final TwinService twinService;
    private final TwinCreateRqRestDTOReverseMapper twinCreateRqRestDTOReverseMapper;


    /**
     * Endpoint for validating a new twin from a JSON request body.
     */
    @ParametersApiUserHeaders
    @Operation(summary = "twinCreatedValidateV1", description = "Validates a twin using a standard JSON payload.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TwinChangeResponse.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")
    })
    @PostMapping(value = "/private/twin/v2/validate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinValidate(@RequestBody TwinCreateRqDTOv2 request) {
        return validateTwin(request);
    }

    protected ResponseEntity<Response> validateTwin(TwinCreateRqDTOv2 request) {
        TwinChangeResponse rs = new TwinChangeResponse();
        try {
            TwinCreate twinCreate = twinCreateRqRestDTOReverseMapper.convert(request);
            twinService.checkFieldsValidity(twinCreate.getTwinEntity(), twinCreate.getFields());
        } catch (TwinFieldValidationException ve) {
            return createErrorRs(ve, rs);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
