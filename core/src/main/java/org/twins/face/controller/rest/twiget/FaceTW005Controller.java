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
import org.twins.face.dao.twidget.tw005.FaceTW005Entity;
import org.twins.face.dto.rest.twidget.tw005.FaceTW005ViewRsDTOv1;
import org.twins.face.mappers.rest.twidget.tw005.FaceTW005RestDTOMapper;
import org.twins.face.service.twidget.FaceTW005Service;

import java.util.UUID;

@Tag(description = "Get TW0005 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceTW005Controller extends ApiController {
    private final FaceTW005Service faceTW005Service;
    private final FaceTW005RestDTOMapper faceTW005RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceTW005ViewV1", summary = "Returns TW005 widget config: transitions buttons widget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TW005 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceTW005ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/tw005/{faceId}/v1")
    public ResponseEntity<?> faceTW005ViewV1(
            @MapperContextBinding(roots = FaceTW005RestDTOMapper.class, response = FaceTW005ViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam UUID twinId) {
        FaceTW005ViewRsDTOv1 rs = new FaceTW005ViewRsDTOv1();
        try {
            PointedFace<FaceTW005Entity> config = faceTW005Service.findPointedFace(faceId, twinId);
            rs
                    .setWidget(faceTW005RestDTOMapper.convert(config, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
