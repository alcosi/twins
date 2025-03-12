package org.twins.core.controller.rest.priv.face;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.face.FaceViewRsDTOv1;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.face.FaceService;

import java.util.UUID;

@Tag(description = "Get card list", name = ApiTag.CARD)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceViewController extends ApiController {
    private final FaceService faceService;
    private final FaceRestDTOMapper faceRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceViewV1", summary = "Returns face details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin card list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceViewRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/{faceId}/v1")
    public ResponseEntity<?> faceViewV1(
            @MapperContextBinding(roots = FaceRestDTOMapper.class, response = FaceViewRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_CONFIG_ID) @PathVariable UUID faceId) {
        FaceViewRsDTOv1 rs = new FaceViewRsDTOv1();
        try {
            rs.setFace(
                    faceRestDTOMapper.convert(
                            faceService.findEntitySafe(faceId), mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
