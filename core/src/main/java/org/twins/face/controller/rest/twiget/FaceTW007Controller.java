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
import org.twins.face.dao.twidget.tw007.FaceTW007Entity;
import org.twins.face.dto.rest.twidget.tw007.FaceTW007ViewRsDTOv1;
import org.twins.face.mappers.rest.twidget.tw007.FaceTW007RestDTOMapper;
import org.twins.face.service.twidget.FaceTW007Service;

import java.util.UUID;

@Tag(description = "Get TW0007 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceTW007Controller extends ApiController {

    private final FaceTW007Service faceTW007Service;
    private final FaceTW007RestDTOMapper faceTW007RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceTW007ViewV1", summary = "Returns TW007 widget config: twin class change widget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TW007 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceTW007ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/tw007/{faceId}/v1")
    public ResponseEntity<?> faceTW007ViewV1(
            @MapperContextBinding(roots = FaceTW007RestDTOMapper.class, response = FaceTW007ViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam UUID twinId) {
        FaceTW007ViewRsDTOv1 rs = new FaceTW007ViewRsDTOv1();

        try {
            PointedFace<FaceTW007Entity> config = faceTW007Service.findPointedFace(faceId, twinId);

            rs
                    .setWidget(faceTW007RestDTOMapper.convert(config, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
