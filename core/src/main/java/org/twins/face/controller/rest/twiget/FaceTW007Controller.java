package org.twins.face.controller.rest.twiget;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.face.dto.rest.twidget.FaceTW007ViewRsDTOv1;

import java.util.UUID;

@Tag(description = "Get TW0007 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceTW007Controller extends ApiController {

    @ParametersApiUserHeaders
    @Operation(operationId = "faceTW007ViewV1", summary = "Returns TW007 widget config: twin class search widget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TW007 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceTW007ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/tw007/{faceId}/v1")
    public ResponseEntity<?> faceTW007ViewV1(
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam UUID twinId) {
        FaceTW007ViewRsDTOv1 rs = new FaceTW007ViewRsDTOv1();
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
