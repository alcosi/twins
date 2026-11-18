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
import org.twins.face.dao.widget.wt007.FaceWT007Entity;
import org.twins.face.dto.rest.widget.wt007.FaceWT007ViewRsDTOv1;
import org.twins.face.mappers.rest.widget.wt007.FaceWT007RestDTOMapper;
import org.twins.face.service.widget.FaceWT007Service;

import java.util.UUID;

@Tag(description = "Get WT007 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceWT007Controller extends ApiController {
    private final FaceWT007Service faceWT007Service;
    private final FaceWT007RestDTOMapper faceWT007RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceWT007ViewV1", summary = "Returns WT007 widget config: transitions buttons widget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WT007 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceWT007ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/wt007/{faceId}/v1")
    public ResponseEntity<?> faceWT007ViewV1(
            @MapperContextBinding(roots = FaceWT007RestDTOMapper.class, response = FaceWT007ViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam UUID twinId) {
        FaceWT007ViewRsDTOv1 rs = new FaceWT007ViewRsDTOv1();
        try {
            PointedFace<FaceWT007Entity> config = faceWT007Service.findPointedFace(faceId, twinId);
            rs
                    .setWidget(faceWT007RestDTOMapper.convert(config, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
