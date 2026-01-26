package org.twins.face.controller.rest.bc;

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
import org.twins.face.dao.bc.FaceBC001Entity;
import org.twins.face.dto.rest.bc.FaceBC001ViewRsDTOv1;
import org.twins.face.dto.rest.twidget.tw001.FaceTW001ViewRsDTOv1;
import org.twins.face.mappers.rest.bc.FaceBC001RestDTOMapper;
import org.twins.face.service.bc.FaceBC001Service;

import java.util.UUID;

@Tag(description = "Get BC0001 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceBC001Controller extends ApiController {

    private final FaceBC001Service faceBC001Service;
    private final FaceBC001RestDTOMapper faceBC001RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceBC001ViewV1", summary = "Returns BC001 widget config: breadcrumbs items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "BC001 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceBC001ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/bc001/{faceId}/v1")
    public ResponseEntity<?> faceBC001ViewV1(
            @MapperContextBinding(roots = FaceBC001RestDTOMapper.class, response = FaceBC001ViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam UUID twinId) {
        FaceBC001ViewRsDTOv1 rs = new FaceBC001ViewRsDTOv1();
        try {
            FaceBC001Entity faceBC001Entity = faceBC001Service.findSingleVariant(faceId, twinId);

            rs
                    .setBreadCrumbs(faceBC001RestDTOMapper.convert(faceBC001Entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
