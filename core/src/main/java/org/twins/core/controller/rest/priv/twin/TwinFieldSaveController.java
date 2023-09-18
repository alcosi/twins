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
import org.twins.core.dto.rest.twin.TwinFieldUpdateRqDTOv1;
import org.twins.core.dto.rest.twin.TwinFieldUpdateRsDTOv1;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapperV2;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

@Tag(description = "", name = "twin")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinFieldSaveController extends ApiController {
    private final AuthService authService;
    private final TwinService twinService;
    private final TwinFieldValueRestDTOReverseMapper twinFieldValueRestDTOReverseMapper;
    private final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinFieldUpdateV1", summary = "Updates twin field data by id (only for existed fields)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinFieldUpdateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_field/{twinFieldId}/v1", method = RequestMethod.PUT)
    public ResponseEntity<?> twinFieldUpdateV1(
            @Parameter(name = "twinFieldId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_FIELD_ID) @PathVariable UUID twinFieldId,
            @RequestBody TwinFieldUpdateRqDTOv1 request) {
        TwinFieldRsDTOv1 rs = new TwinFieldRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            twinService.updateField(twinFieldId, twinFieldValueRestDTOReverseMapper.convert(request.value));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinFieldUpdateV2", summary = "Updates twin field data by id (only for existed fields)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinFieldUpdateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_field/{twinFieldId}/v2", method = RequestMethod.PUT)
    public ResponseEntity<?> twinFieldUpdateV2(
            @Parameter(name = "twinFieldId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_FIELD_ID) @PathVariable UUID twinFieldId,
            @Parameter(name = "fieldValue", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_FIELD_KEY) @RequestParam String fieldValue) {
        TwinFieldRsDTOv1 rs = new TwinFieldRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            TwinFieldEntity twinFieldEntity = twinService.findTwinField(twinFieldId);
            twinService.updateField(twinFieldEntity, twinFieldValueRestDTOReverseMapperV2
                    .convert(
                            twinFieldValueRestDTOReverseMapperV2.createValueByTwinFieldId(twinFieldId, fieldValue)
                    ));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }


    @ParametersApiUserHeaders
    @Operation(operationId = "twinFieldByKeySaveV1", summary = "Creates or updates twin field data by key. ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinFieldRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/{twinId}/field/{fieldKey}/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinFieldSaveV1(
            @Parameter(name = "twinId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Parameter(name = "fieldKey", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_FIELD_KEY) @PathVariable String fieldKey,
            @RequestBody TwinFieldUpdateRqDTOv1 request) {
        TwinFieldRsDTOv1 rs = new TwinFieldRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            TwinFieldEntity twinFieldEntity = twinService.findTwinFieldIncludeMissing(twinId, fieldKey);
            twinService.updateField(twinFieldEntity, twinFieldValueRestDTOReverseMapper.convert(request.value));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinFieldByKeySaveV2", summary = "Creates or updates twin field data by key. ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinFieldRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/{twinId}/field/{fieldKey}/v2", method = RequestMethod.POST)
    public ResponseEntity<?> twinFieldByKeySaveV2(
            @Parameter(name = "twinId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Parameter(name = "fieldKey", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_FIELD_KEY) @PathVariable String fieldKey,
            @Parameter(name = "fieldValue", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_FIELD_KEY) @RequestParam String fieldValue) {
        TwinFieldRsDTOv1 rs = new TwinFieldRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            TwinFieldEntity twinFieldEntity = twinService.findTwinFieldIncludeMissing(twinId, fieldKey);
            twinService.updateField(twinFieldEntity, twinFieldValueRestDTOReverseMapperV2.convert(
                    twinFieldValueRestDTOReverseMapperV2.createByTwinIdAndFieldKey(twinId, fieldKey, fieldValue)
            ));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
