package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFieldRuleService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_FIELD_RULE_MANAGE, Permissions.TWIN_CLASS_FIELD_RULE_DELETE})
public class TwinClassFieldRuleDeleteController extends ApiController {

    private final TwinClassFieldRuleService twinClassFieldRuleService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldRuleDeleteV1", summary = "Delete all field rules for the specified Twin-Class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rules were deleted", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @DeleteMapping(value = "/private/twin_class/{twinClassId}/field_rule/v1")
    public ResponseEntity<?> twinClassFieldRuleDeleteV1(
            @Parameter(example = DTOExamples.TWIN_CLASS_ID)
            @PathVariable UUID twinClassId) {
        Response rs = new Response();
        try {
            twinClassFieldRuleService.deleteRulesByTwinClass(twinClassId);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
