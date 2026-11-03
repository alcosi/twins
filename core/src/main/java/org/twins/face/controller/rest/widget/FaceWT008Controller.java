package org.twins.face.controller.rest.widget;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.face.dto.rest.widget.wt008.FaceWT008ViewRsDTOv1;
import org.twins.face.mappers.rest.widget.wt008.FaceWT008RestDTOMapper;
import org.twins.face.service.widget.FaceWT008Service;

import java.util.UUID;

@Tag(description = "Get WT008 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceWT008Controller extends ApiController {
    private final FaceWT008Service faceWT008Service;
    private final FaceWT008RestDTOMapper faceWT008RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceWT008ViewV1", summary = "Returns WT008 widget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WT008 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceWT008ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/wt008/{faceId}/v1")
    public ResponseEntity<?> faceWT008ViewV1(
            @MapperContextBinding(roots = FaceWT008RestDTOMapper.class, response = FaceWT008ViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam(required = false) UUID twinId) {
        FaceWT008ViewRsDTOv1 rs = new FaceWT008ViewRsDTOv1();
        try {
            var faceWT008 = faceWT008Service.findSingleVariant(faceId, twinId);
            rs
                    .setWidget(faceWT008RestDTOMapper.convert(faceWT008, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
