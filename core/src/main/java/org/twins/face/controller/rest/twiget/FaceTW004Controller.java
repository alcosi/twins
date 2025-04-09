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
import org.twins.core.domain.face.TwidgetConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.face.dao.twidget.tw004.FaceTW004Entity;
import org.twins.face.dto.rest.twidget.tw004.FaceTW004ViewRsDTOv1;
import org.twins.face.mappers.rest.twidget.tw004.FaceTW004RestDTOMapper;
import org.twins.face.service.twidget.FaceTW004Service;

import java.util.UUID;

@Tag(description = "Get TW0004 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceTW004Controller extends ApiController {
    private final FaceTW004Service faceTW004Service;
    private final FaceTW004RestDTOMapper faceTW004RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceTW004ViewV1", summary = "Returns TW004 widget config: twins single field view/edit widget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TW004 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceTW004ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/tw004/{faceId}/v1")
    public ResponseEntity<?> faceTW004ViewV1(
            @MapperContextBinding(roots = FaceTW004RestDTOMapper.class, response = FaceTW004ViewRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam UUID twinId) {
        FaceTW004ViewRsDTOv1 rs = new FaceTW004ViewRsDTOv1();
        try {
            TwidgetConfig<FaceTW004Entity> config = faceTW004Service.getConfig(faceId, twinId);
            rs
                    .setWidget(faceTW004RestDTOMapper.convert(config, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
