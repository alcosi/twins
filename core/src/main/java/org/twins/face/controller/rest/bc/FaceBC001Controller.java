package org.twins.face.controller.rest.bc;

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
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.face.dto.rest.bc.FaceBC001ViewRsDTOv1;
import org.twins.face.dto.rest.twidget.tw001.FaceTW001ViewRsDTOv1;

import java.util.UUID;

@Tag(description = "Get BC0001 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceBC001Controller {

    @ParametersApiUserHeaders
    @Operation(operationId = "faceBC001ViewV1", summary = "Returns BC001 widget config: breadcrumbs items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "BC001 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceTW001ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/bc001/{faceId}/v1")
    public ResponseEntity<?> faceBC001ViewV1(
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam UUID twinId) {
        FaceBC001ViewRsDTOv1 rs = new FaceBC001ViewRsDTOv1();
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
