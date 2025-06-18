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
import org.twins.face.dao.twidget.tw002.FaceTW002Entity;
import org.twins.face.dto.rest.twidget.tw002.FaceTW002ViewRsDTOv1;
import org.twins.face.mappers.rest.twidget.tw002.FaceTW002RestDTOMapper;
import org.twins.face.service.twidget.FaceTW002Service;

import java.util.UUID;

@Tag(description = "Get TW002 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceTW002Controller extends ApiController {
    private final FaceTW002Service faceTW002Service;
    private final FaceTW002RestDTOMapper faceTW002RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceTW002ViewV1", summary = "Returns TW002 widget config: i18n field accordion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TW002 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceTW002ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/tw002/{faceId}/v1")
    public ResponseEntity<?> faceTW002ViewV1(
            @MapperContextBinding(roots = FaceTW002RestDTOMapper.class, response = FaceTW002ViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam UUID twinId) {
        FaceTW002ViewRsDTOv1 rs = new FaceTW002ViewRsDTOv1();
        try {
            TwidgetConfig<FaceTW002Entity> config = faceTW002Service.getConfig(faceId, twinId);
            rs
                    .setWidget(faceTW002RestDTOMapper.convert(config, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
