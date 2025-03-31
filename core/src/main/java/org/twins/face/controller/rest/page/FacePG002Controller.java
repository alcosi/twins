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
import org.twins.face.dao.page.pg002.FacePG002Entity;
import org.twins.face.dto.rest.page.pg002.FacePG002ViewRsDTOv1;
import org.twins.face.mappers.rest.page.pg002.FacePG002RestDTOMapper;
import org.twins.face.service.page.FacePG002Service;

import java.util.UUID;

@Tag(description = "Get PG002 face config by id", name = ApiTag.FACE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FacePG002Controller extends ApiController {
    private final FacePG002Service facePG002Service;
    private final FacePG002RestDTOMapper facePG002RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "facePG002ViewV1", summary = "Returns PG002 page config: tabs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PG002 face config", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FacePG002ViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/face/pg002/{faceId}/v1")
    public ResponseEntity<?> facePG002ViewV1(
            @MapperContextBinding(roots = FacePG002RestDTOMapper.class, response = FacePG002ViewRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACE_ID) @PathVariable UUID faceId) {
        FacePG002ViewRsDTOv1 rs = new FacePG002ViewRsDTOv1();
        try {
            FacePG002Entity facePG002Entity = facePG002Service.findEntitySafe(faceId);
            rs
                    .setPage(facePG002RestDTOMapper.convert(facePG002Entity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
