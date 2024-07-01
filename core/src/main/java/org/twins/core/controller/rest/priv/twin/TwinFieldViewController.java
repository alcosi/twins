package org.twins.core.controller.rest.priv.twin;

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
import org.twins.core.domain.TwinField;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinFieldRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinFieldViewController extends ApiController {
    private final AuthService authService;
    private final TwinService twinService;
    private final TwinFieldRestDTOMapper twinFieldRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinFieldByKeyViewV1", summary = "Returns twin field data by key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinFieldRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin/{twinId}/field/{fieldKey}/v1")
    public ResponseEntity<?> twinFieldByKeyViewV1(
            @MapperContextBinding(roots = TwinFieldRestDTOMapper.class, response = TwinFieldRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Parameter(example = DTOExamples.TWIN_FIELD_KEY) @PathVariable String fieldKey) {
        TwinFieldRsDTOv1 rs = new TwinFieldRsDTOv1();
        try {
            TwinField twinField = twinService.wrapField(twinId, fieldKey);
            fillResponse(twinField, mapperContext, rs);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    private void fillResponse(TwinField twinField, MapperContext mapperContext, TwinFieldRsDTOv1 rs) throws Exception {
        rs
                .twinId(twinField.getTwinId())
                .field(twinFieldRestDTOMapper.convert(
                        twinField, mapperContext
                ));
    }
}
