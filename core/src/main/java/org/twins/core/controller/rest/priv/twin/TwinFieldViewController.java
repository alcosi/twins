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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinFieldRsDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

@Tag(description = "", name = "twin")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinFieldViewController extends ApiController {
    private final AuthService authService;
    private final TwinService twinService;
    private final TwinFieldValueRestDTOMapper twinFieldValueRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinFieldViewV1", summary = "Returns twin field data by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinFieldRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_field/{twinFieldId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinFieldViewV1(
            @Parameter(name = "twinFieldId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_FIELD_ID) @PathVariable UUID twinFieldId,
            @Parameter(name = "showTwinValuesMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinFieldValueRestDTOMapper.Mode._FIELDS_KEY_VALUE_ONLY) TwinFieldValueRestDTOMapper.Mode showTwinValuesMode) {
        TwinFieldRsDTOv1 rs = new TwinFieldRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            TwinFieldEntity twinFieldEntity = twinService.findTwinField(twinFieldId);
            fillResponse(twinFieldEntity, showTwinValuesMode, rs);
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
            @Parameter(name = "twinId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Parameter(name = "fieldKey", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_FIELD_KEY) @PathVariable String fieldKey,
            @Parameter(name = "showTwinValuesMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinFieldValueRestDTOMapper.Mode._FIELDS_KEY_VALUE_ONLY) TwinFieldValueRestDTOMapper.Mode showTwinValuesMode) {
        TwinFieldRsDTOv1 rs = new TwinFieldRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            TwinFieldEntity twinFieldEntity = twinService.findTwinFieldIncludeMissing(twinId, fieldKey);
            fillResponse(twinFieldEntity, showTwinValuesMode, rs);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    private void fillResponse(TwinFieldEntity twinFieldEntity, TwinFieldValueRestDTOMapper.Mode showTwinValuesMode, TwinFieldRsDTOv1 rs) throws Exception {
        rs
                .twinId(twinFieldEntity.twinId())
                .field(twinFieldValueRestDTOMapper.convert(
                        twinFieldEntity, new MapperProperties()
                                .setMode(showTwinValuesMode)
                ));
    }
}
