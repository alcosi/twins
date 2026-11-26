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
import org.twins.core.domain.face.PointedFace;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.face.dao.widget.wt011.FaceWT011Entity;
import org.twins.face.dto.rest.widget.wt011.FaceWT011ViewRsDTOv1;
import org.twins.face.mappers.rest.widget.wt011.FaceWT011RestDTOMapper;
import org.twins.face.service.widget.FaceWT011Service;

import java.util.UUID;

@Tag(description = "Get WT011 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceWT011Controller extends ApiController {
    private final FaceWT011Service faceWT011Service;
    private final FaceWT011RestDTOMapper faceWT011RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceWT011ViewV1", summary = "Returns WT011 widget config: twin form widget (columns/sections/rows/fields layout)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WT011 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceWT011ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/wt011/{faceId}/v1")
    public ResponseEntity<?> faceWT011ViewV1(
            @MapperContextBinding(roots = FaceWT011RestDTOMapper.class, response = FaceWT011ViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam UUID twinId) {
        FaceWT011ViewRsDTOv1 rs = new FaceWT011ViewRsDTOv1();
        try {
            PointedFace<FaceWT011Entity> pointedFace = faceWT011Service.findPointedFace(faceId, twinId);
            rs
                    .setWidget(faceWT011RestDTOMapper.convert(pointedFace, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
