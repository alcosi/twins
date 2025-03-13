package org.twins.core.controller.rest.priv.face.nb001;

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
import org.twins.core.dao.face.navbar.FaceNB001Entity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.face.navbar.nb001.FaceNB001ViewRsDTOv1;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.face.navbar.FaceNB001RestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.face.navbar.FaceNB001Service;

import java.util.UUID;

@Tag(description = "Get face by id", name = ApiTag.CARD)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceNB001Controller extends ApiController {
    private final FaceNB001Service faceNB001Service;
    private final FaceNB001RestDTOMapper faceNB001RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceNB001ViewV1", summary = "Returns nb001 navigation bar details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin card list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceNB001ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/nb001/{faceId}/v1")
    public ResponseEntity<?> faceNB001ViewV1(
            @MapperContextBinding(roots = FaceRestDTOMapper.class, response = FaceNB001ViewRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId) {
        FaceNB001ViewRsDTOv1 rs = new FaceNB001ViewRsDTOv1();
        try {
            FaceNB001Entity faceNB001Entity = faceNB001Service.findEntitySafe(faceId);
            rs
                    .setNavbar(faceNB001RestDTOMapper.convert(faceNB001Entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
