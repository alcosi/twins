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
import org.twins.face.dao.widget.wt003.FaceWT003Entity;
import org.twins.face.dto.rest.widget.wt003.FaceWT003ViewRsDTOv1;
import org.twins.face.mappers.rest.widget.wt003.FaceWT003RestDTOMapper;
import org.twins.face.service.widget.FaceWT003Service;

import java.util.UUID;

@Tag(description = "Get WT0003 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceWT003Controller extends ApiController {
    private final FaceWT003Service faceWT003Service;
    private final FaceWT003RestDTOMapper faceWT003RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceWT003ViewV1", summary = "Returns WT003 widget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WT003 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceWT003ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/wt003/{faceId}/v1")
    public ResponseEntity<?> faceWT003ViewV1(
            @MapperContextBinding(roots = FaceWT003RestDTOMapper.class, response = FaceWT003ViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam(required = false) UUID twinId) {
        FaceWT003ViewRsDTOv1 rs = new FaceWT003ViewRsDTOv1();
        try {
            FaceWT003Entity faceWT003Entity = faceWT003Service.findEntitySafe(faceId);
            rs
                    .setWidget(faceWT003RestDTOMapper.convert(faceWT003Entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
