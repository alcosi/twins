package org.twins.core.controller.rest.priv.twin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinFieldRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
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
    @Operation(operationId = "twinFieldViewV1", summary = "Returns twin field data by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinFieldRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_field/{twinFieldId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinFieldViewV1(
            @Parameter(example = DTOExamples.TWIN_FIELD_ID) @PathVariable UUID twinFieldId,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showTwinClassFieldMode) {
        TwinFieldRsDTOv1 rs = new TwinFieldRsDTOv1();
        try {
            TwinFieldEntity twinFieldEntity = twinService.findTwinField(twinFieldId);
            fillResponse(twinFieldEntity, showTwinClassFieldMode, rs);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinFieldByKeyViewV1", summary = "Returns twin field data by key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinFieldRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/{twinId}/field/{fieldKey}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinFieldByKeyViewV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Parameter(example = DTOExamples.TWIN_FIELD_KEY) @PathVariable String fieldKey,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showTwinClassFieldMode) {
        TwinFieldRsDTOv1 rs = new TwinFieldRsDTOv1();
        try {
            TwinFieldEntity twinFieldEntity = twinService.findTwinFieldIncludeMissing(twinId, fieldKey);
            fillResponse(twinFieldEntity, showTwinClassFieldMode, rs);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    private void fillResponse(TwinFieldEntity twinFieldEntity, TwinClassFieldRestDTOMapper.Mode showClassFieldMode, TwinFieldRsDTOv1 rs) throws Exception {
        rs
                .twinId(twinFieldEntity.getTwinId())
                .field(twinFieldRestDTOMapper.convert(
                        twinFieldEntity, new MapperContext()
                                .setMode(showClassFieldMode)
                ));
    }
}
