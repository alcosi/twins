package org.twins.face.controller.rest.twiget;

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
import org.twins.face.dao.twidget.tw001.FaceTW001Entity;
import org.twins.face.dto.rest.twidget.tw001.FaceTW001ViewRsDTOv1;
import org.twins.face.mappers.rest.twidget.tw001.FaceTW001RestDTOMapper;
import org.twins.face.service.twidget.FaceTW001Service;

import java.util.UUID;

@Tag(description = "Get TW0001 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceTW001Controller extends ApiController {
    private final FaceTW001Service faceTW001Service;
    private final FaceTW001RestDTOMapper faceTW001RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceTW001ViewV1", summary = "Returns TW001 widget config: image gallery")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TW001 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceTW001ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/tw001/{faceId}/v1")
    public ResponseEntity<?> faceTW001ViewV1(
            @MapperContextBinding(roots = FaceTW001RestDTOMapper.class, response = FaceTW001ViewRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam UUID twinId) {
        FaceTW001ViewRsDTOv1 rs = new FaceTW001ViewRsDTOv1();
        try {
            PointedFace<FaceTW001Entity> config = faceTW001Service.findPointedFace(faceId, twinId);
            rs
                    .setWidget(faceTW001RestDTOMapper.convert(config, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
