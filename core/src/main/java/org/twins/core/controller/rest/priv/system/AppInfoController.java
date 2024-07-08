package org.twins.core.controller.rest.priv.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.system.AppInfoRsDTOv1;
import org.twins.core.service.system.system.AppInfoService;

@Tag(description = "Application info form MANIFEST.MF", name = ApiTag.SYSTEM)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AppInfoController extends ApiController {
    private final AppInfoService appInfoService;

    @ParametersApiUserHeaders
    @Operation(operationId = "Application info", summary = "Returns application info: versions, etc...")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application info", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AppInfoRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/system/info")
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> getAppInfo() {
        AppInfoRsDTOv1 rs = new AppInfoRsDTOv1();
        try {
            rs.setAttributes(appInfoService.getManifestAttributes());
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
