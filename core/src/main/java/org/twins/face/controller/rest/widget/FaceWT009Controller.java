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
import org.twins.face.dao.widget.wt009.FaceWT009Entity;
import org.twins.face.dto.rest.widget.wt009.FaceWT009ViewRsDTOv1;
import org.twins.face.mappers.rest.widget.wt009.FaceWT009RestDTOMapper;
import org.twins.face.service.widget.FaceWT009Service;

import java.util.UUID;

@Tag(description = "Get WT009 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceWT009Controller extends ApiController {

    private final FaceWT009Service faceWT009Service;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final FaceWT009RestDTOMapper faceWT009RestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceWT009ViewV1", summary = "Returns WT009 widget config: twin actions widget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WT009 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceWT009ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/wt009/{faceId}/v1")
    public ResponseEntity<?> faceWT009ViewV1(
            @MapperContextBinding(roots = FaceWT009RestDTOMapper.class, response = FaceWT009ViewRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam UUID twinId) {
        FaceWT009ViewRsDTOv1 rs = new FaceWT009ViewRsDTOv1();
        try {
            PointedFace<FaceWT009Entity> config = faceWT009Service.findPointedFace(faceId, twinId);

            rs
                    .setWidget(faceWT009RestDTOMapper.convert(config, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
