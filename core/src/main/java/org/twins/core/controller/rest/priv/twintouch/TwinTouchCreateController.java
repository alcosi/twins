package org.twins.core.controller.rest.priv.twintouch;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinTouchEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinTouchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twin.TwinTouchRestDTOMapper;
import org.twins.core.service.twin.TwinTouchService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinTouchCreateController extends ApiController {

    private final TwinTouchRestDTOMapper twinTouchRestDTOMapper;

    private final TwinTouchService twinTouchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinTouchV1", summary = "Mark twin as touched for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinTouchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin/{twinId}/touch/{touchId}/v1")
    public ResponseEntity<?> twinTouchV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Parameter(example = DTOExamples.TWIN_TOUCH) @PathVariable String touchId,
            @MapperContextBinding(roots = TwinTouchRestDTOMapper.class, response = TwinTouchRsDTOv1.class) MapperContext mapperContext) {
        TwinTouchRsDTOv1 rs = new TwinTouchRsDTOv1();
        try {
            TwinTouchEntity twinTouchEntity = twinTouchService.addTouch(twinId, touchId);
            rs
                    .twinTouch(twinTouchRestDTOMapper.convert(twinTouchEntity, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
