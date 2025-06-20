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
import org.twins.face.dao.tc.tc001.FaceTC001Entity;
import org.twins.face.domain.tc.tc001.FaceTC001Twin;
import org.twins.face.dto.rest.tc.tc001.FaceTC001ViewRsDTOv1;
import org.twins.face.mappers.rest.tc.tc001.FaceTC001RestDTOMapper;
import org.twins.face.service.tc.FaceTC001Service;

import java.util.UUID;

@Tag(description = "Get TC0001 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceTC001Controller extends ApiController {
    private final FaceTC001Service faceTC001Service;
    private final FaceTC001RestDTOMapper faceTC001RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceTC001ViewV1", summary = "Returns TC001 widget config: twin create buttons widget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TC002 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceTC001ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/tc001/{faceId}/v1")
    public ResponseEntity<?> faceTC001ViewV1(
            @MapperContextBinding(roots = FaceTC001RestDTOMapper.class, response = FaceTC001ViewRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam(required = false) UUID twinId) {
        FaceTC001ViewRsDTOv1 rs = new FaceTC001ViewRsDTOv1();
        try {
            FaceTC001Twin faceTC001Twin = new FaceTC001Twin(faceTC001Service.findEntitySafe(faceId), twinId);
            rs
                    .setFaceTwinCreate(faceTC001RestDTOMapper.convert(faceTC001Twin, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
