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
import org.twins.face.dao.widget.wt001.FaceWT001Entity;
import org.twins.face.dto.rest.widget.wt001.FaceWT001ViewRsDTOv1;
import org.twins.face.mappers.rest.widget.wt001.FaceWT001RestDTOMapper;
import org.twins.face.service.widget.FaceWT001Service;

import java.util.UUID;

@Tag(description = "Get face by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceWT001Controller extends ApiController {
    private final FaceWT001Service faceWT001Service;
    private final FaceWT001RestDTOMapper faceWT001RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceWT001ViewV1", summary = "Returns WT001 widget config: table of twins of given class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WT001 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceWT001ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/wt001/{faceId}/v1")
    public ResponseEntity<?> faceWT001ViewV1(
            @MapperContextBinding(roots = FaceWT001RestDTOMapper.class, response = FaceWT001ViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam(required = false) UUID twinId) {
        FaceWT001ViewRsDTOv1 rs = new FaceWT001ViewRsDTOv1();
        try {
            FaceWT001Entity faceWT001Entity = faceWT001Service.findEntitySafe(faceId);
            rs
                    .setWidget(faceWT001RestDTOMapper.convert(faceWT001Entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
