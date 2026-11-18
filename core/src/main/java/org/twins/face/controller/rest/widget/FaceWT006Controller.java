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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.face.dao.widget.wt006.FaceWT006Entity;
import org.twins.face.dto.rest.widget.wt006.FaceWT006ViewRsDTOv1;
import org.twins.face.dto.rest.widget.wt006.FaceWT006ViewRsDTOv2;
import org.twins.face.mappers.rest.widget.wt006.FaceWT006RestDTOMapper;
import org.twins.face.mappers.rest.widget.wt006.FaceWT006v2RestDTOMapper;
import org.twins.face.service.widget.FaceWT006Service;

import java.util.UUID;

@Tag(description = "Get WT006 config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FaceWT006Controller extends ApiController {
    private final FaceWT006Service faceWT006Service;
    private final FaceWT006RestDTOMapper faceWT006RestDTOMapper;
    private final FaceWT006v2RestDTOMapper faceWT006RestDTOMapperV2;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @Deprecated
    @ParametersApiUserHeaders
    @Operation(operationId = "faceWT006ViewV1", summary = "Returns WT006 widget config: twins single field view/edit widget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WT006 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceWT006ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/wt006/{faceId}/v1")
    public ResponseEntity<?> faceWT006ViewV1(
            @MapperContextBinding(roots = FaceWT006RestDTOMapper.class, response = FaceWT006ViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam UUID twinId) {
        FaceWT006ViewRsDTOv1 rs = new FaceWT006ViewRsDTOv1();
        try {
            PointedFace<FaceWT006Entity> config = faceWT006Service.findPointedFace(faceId, twinId);
            rs
                    .setWidget(faceWT006RestDTOMapper.convert(config, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "faceWT006ViewV2", summary = "Returns WT006 widget config: twins multi field view/edit widget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WT006 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceWT006ViewRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/wt006/{faceId}/v2")
    public ResponseEntity<?> faceWT006ViewV2(
            @MapperContextBinding(roots = FaceWT006v2RestDTOMapper.class, response = FaceWT006ViewRsDTOv2.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId,
            @RequestParam UUID twinId) {
        FaceWT006ViewRsDTOv2 rs = new FaceWT006ViewRsDTOv2();
        try {
            PointedFace<FaceWT006Entity> config = faceWT006Service.findPointedFace(faceId, twinId);
            rs
                    .setWidget(faceWT006RestDTOMapperV2.convert(config, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
