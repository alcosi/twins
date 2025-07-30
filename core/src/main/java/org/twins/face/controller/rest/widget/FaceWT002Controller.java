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
import org.twins.face.dao.widget.wt002.FaceWT002Entity;
import org.twins.face.dto.rest.widget.wt002.FaceWT002ViewRsDTOv1;
import org.twins.face.mappers.rest.widget.wt002.FaceWT002RestDTOMapper;
import org.twins.face.service.widget.FaceWT002Service;

import java.util.UUID;

@Tag(description = "Get WT0002 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceWT002Controller extends ApiController {
    private final FaceWT002Service faceWT002Service;
    private final FaceWT002RestDTOMapper faceWT002RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceWT002ViewV1", summary = "Returns WT002 widget config: twin create buttons widget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WT002 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceWT002ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/wt002/{faceId}/v1")
    public ResponseEntity<?> faceWT002ViewV1(
            @MapperContextBinding(roots = FaceWT002RestDTOMapper.class, response = FaceWT002ViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam(required = false) UUID twinId) {
        FaceWT002ViewRsDTOv1 rs = new FaceWT002ViewRsDTOv1();
        try {
            FaceWT002Entity faceWT001Entity = faceWT002Service.findSingleVariant(faceId, twinId);
            rs
                    .setWidget(faceWT002RestDTOMapper.convert(faceWT001Entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
