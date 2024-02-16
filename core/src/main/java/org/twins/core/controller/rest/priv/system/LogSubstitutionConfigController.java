package org.twins.core.controller.rest.priv.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.system.CommandRsDTOv1;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.system.LogSupportService;

@Tag(description = "Config substitution for log file", name = ApiTag.SYSTEM)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class LogSubstitutionConfigController extends ApiController {
    final LogSupportService logSupportService;
    final AuthService authService;

    @ParametersApiUserHeaders
    @Operation(operationId = "makeConfigForSubstitutions", summary = "Make config file for log substitutions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Run command", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommandRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/system/log/substitutions_config/v1")
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> makeConfigForSubstitutions(@Nullable @RequestParam(name = RestRequestParam.filename, required = false) String filename) {
        CommandRsDTOv1 rs = new CommandRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.setCommand(logSupportService.generateSubstitutionsConfig(apiUser, filename));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
