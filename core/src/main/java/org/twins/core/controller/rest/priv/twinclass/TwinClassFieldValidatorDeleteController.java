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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twinclass.TwinClassFieldValidatorDeleteRqDTOv1;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclassfield.TwinClassFieldValidatorService;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.TWIN_CLASS_FIELD_DELETE)
public class TwinClassFieldValidatorDeleteController extends ApiController {
    private final TwinClassFieldValidatorService twinClassFieldValidatorService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldValidatorDeleteV1", summary = "Twin class field validator batch delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deletion result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_field_validator/delete/v1")
    public ResponseEntity<?> twinClassFieldValidatorDeleteV1(
            @RequestBody TwinClassFieldValidatorDeleteRqDTOv1 request) {
        Response rs = new Response();
        try {
            twinClassFieldValidatorService.deleteSafe(request.getTwinClassFieldValidatorIdList());
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
