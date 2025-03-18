package org.twins.face.controller.rest.page;

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
import org.twins.face.dao.page.FacePG001Entity;
import org.twins.face.dto.rest.page.FacePG001ViewRsDTOv1;
import org.twins.face.mappers.rest.page.FacePG001RestDTOMapper;
import org.twins.face.service.page.FacePG001Service;

import java.util.UUID;

@Tag(description = "Get face by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FacePG001Controller extends ApiController {
    private final FacePG001Service facePG001Service;
    private final FacePG001RestDTOMapper facePG001RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "facePG001ViewV1", summary = "Returns pg001 navigation bar details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PG001 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FacePG001ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/pg001/{faceId}/v1")
    public ResponseEntity<?> facePG001ViewV1(
            @MapperContextBinding(roots = FacePG001RestDTOMapper.class, response = FacePG001ViewRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId) {
        FacePG001ViewRsDTOv1 rs = new FacePG001ViewRsDTOv1();
        try {
            FacePG001Entity facePG001Entity = facePG001Service.findEntitySafe(faceId);
            rs
                    .setPage(facePG001RestDTOMapper.convert(facePG001Entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
