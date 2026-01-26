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
import org.twins.face.dao.twidget.tw006.FaceTW006Entity;
import org.twins.face.dto.rest.twidget.tw006.FaceTW006DTOv1;
import org.twins.face.dto.rest.twidget.tw006.FaceTW006ViewRsDTOv1;
import org.twins.face.mappers.rest.twidget.tw006.FaceTW006RestDTOMapper;
import org.twins.face.service.twidget.FaceTW006Service;

import java.util.UUID;

@Tag(description = "Get TW0006 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceTW006Controller extends ApiController {

    private final FaceTW006Service faceTW006Service;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final FaceTW006RestDTOMapper faceTW006RestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceTW006ViewV1", summary = "Returns TW006 widget config: twin actions widget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TW006 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceTW006ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/tw006/{faceId}/v1")
    public ResponseEntity<?> faceTW006ViewV1(
            @MapperContextBinding(roots = FaceTW006RestDTOMapper.class, response = FaceTW006ViewRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam UUID twinId) {
        FaceTW006ViewRsDTOv1 rs = new FaceTW006ViewRsDTOv1();
        try {
            PointedFace<FaceTW006Entity> config = faceTW006Service.findPointedFace(faceId, twinId);

            rs
                    .setWidget(faceTW006RestDTOMapper.convert(config, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
