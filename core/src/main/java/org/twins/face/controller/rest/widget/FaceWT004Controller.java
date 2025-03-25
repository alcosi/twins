package org.twins.face.controller.rest.widget;

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
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.face.dao.widget.FaceWT004Entity;
import org.twins.face.dto.rest.widget.FaceWT004ViewRsDTOv1;
import org.twins.face.mappers.rest.widget.FaceWT004RestDTOMapper;
import org.twins.face.service.widget.FaceWT004Service;

import java.util.UUID;

@Tag(description = "Get face by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceWT004Controller extends ApiController {
    private final FaceWT004Service faceWT004Service;
    private final FaceWT004RestDTOMapper faceWT004RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "faceWT004ViewV1", summary = "Returns WT004 widget config: i18n field accordion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WT004 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceWT004ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/wt004/{faceId}/v1")
    public ResponseEntity<?> faceWT004ViewV1(
            @MapperContextBinding(roots = FaceWT004RestDTOMapper.class, response = FaceWT004ViewRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId) {
        FaceWT004ViewRsDTOv1 rs = new FaceWT004ViewRsDTOv1();
        try {
            FaceWT004Entity faceWT004Entity = faceWT004Service.findEntitySafe(faceId);
            rs
                    .setWidget(faceWT004RestDTOMapper.convert(faceWT004Entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
