package org.twins.face.controller.rest.tc;

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
import org.twins.face.dao.tc.tc002.FaceTC002Entity;
import org.twins.face.dto.rest.tc.tc002.FaceTC002ViewRsDTOv1;
import org.twins.face.mappers.rest.tc.tc001.FaceTC001RestDTOMapper;
import org.twins.face.mappers.rest.tc.tc002.FaceTC002RestDTOMapper;
import org.twins.face.service.tc.FaceTC002Service;

import java.util.UUID;

@Tag(description = "Get TC0001 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceTC002Controller extends ApiController {
    private final FaceTC002Service faceTC002Service;
    private final FaceTC002RestDTOMapper faceTC002RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceTC002ViewV1", summary = "Returns TC002 widget config: twin sketch create buttons widget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TC002 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceTC002ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/tc002/{faceId}/v1")
    public ResponseEntity<?> faceTC001ViewV1(
            @MapperContextBinding(roots = FaceTC002RestDTOMapper.class, response = FaceTC002ViewRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam(required = false) UUID twinId) {
        FaceTC002ViewRsDTOv1 rs = new FaceTC002ViewRsDTOv1();
        try {
            FaceTC002Entity faceTC002Entity = faceTC002Service.findSingleVariant(faceId, twinId);
            rs
                    .setFaceTwinCreate(faceTC002RestDTOMapper.convert(faceTC002Entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
