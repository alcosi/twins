package org.twins.core.controller.rest.priv.twinclass;

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
import org.twins.core.domain.twinclass.TwinClassUpdate;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TwinClassRsDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassUpdateRestDTOReverseMapper;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.UUID;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinClassUpdateController extends ApiController {
    private final TwinClassService twinClassService;
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final TwinClassUpdateRestDTOReverseMapper twinClassUpdateRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassUpdateV1", summary = "Update twin class by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin_class/{twinClassId}/v1")
    public ResponseEntity<?> twinClassUpdateV1(
            @MapperContextBinding(roots = TwinClassRestDTOMapper.class, response = TwinClassRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @RequestBody TwinClassUpdateRqDTOv1 request) {
        TwinClassRsDTOv1 rs = new TwinClassRsDTOv1();
        try {
            TwinClassUpdate twinClassUpdate = twinClassUpdateRestDTOReverseMapper.convert(request.setTwinClassId(twinClassId));
            twinClassService.updateTwinClass(twinClassUpdate);
            rs
                    .setTwinClass(twinClassRestDTOMapper.convert(twinClassUpdate.getDbTwinClassEntity(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
