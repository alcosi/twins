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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.twin.*;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinFieldSaveController extends ApiController {
    final AuthService authService;
    final TwinService twinService;
    final TwinFieldValueRestDTOReverseMapper twinFieldValueRestDTOReverseMapper;
    final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    final TwinRestDTOMapperV2 twinRestDTOMapperV2;


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

    @ParametersApiUserHeaders
    @Operation(operationId = "twinFieldListUpdateV1", summary = "Updates twin fields data by keys")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/{twinId}/field_list/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinFieldListUpdateV1(
            @Parameter(name = "twinId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Parameter(name = "showUserMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = UserRestDTOMapper.Mode._ID_ONLY) UserRestDTOMapper.Mode showUserMode,
            @Parameter(name = "showStatusMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinStatusRestDTOMapper.Mode._ID_ONLY) TwinStatusRestDTOMapper.Mode showStatusMode,
            @Parameter(name = "showClassMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.ClassMode._ID_ONLY) TwinClassRestDTOMapper.ClassMode showClassMode,
            @Parameter(name = "showClassFieldListMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.FieldsMode._NO_FIELDS) TwinClassRestDTOMapper.FieldsMode showClassFieldListMode,
            @Parameter(name = "showClassFieldMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassFieldRestDTOMapper.Mode._ID_KEY_ONLY) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @Parameter(name = "showTwinMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.TwinMode._DETAILED) TwinRestDTOMapper.TwinMode showTwinMode,
            @Parameter(name = "showTwinFieldsMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @Parameter(name = "showTwinAttachmentMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.AttachmentsMode._HIDE) TwinRestDTOMapper.AttachmentsMode showTwinAttachmentMode,
            @RequestBody TwinFieldListUpdateRqDTOv1 request) {
        TwinRsDTOv2 rs = new TwinRsDTOv2();
        try {
            ApiUser apiUser = authService.getApiUser();
            TwinEntity twinEntity = twinService.findTwin(apiUser, twinId, EntitySmartService.FindMode.ifEmptyThrows);
            List<FieldValue> fields = new ArrayList<>();
            if (request.getFields() != null)
                for (Map.Entry<String, String> entry : request.getFields().entrySet())
                    fields.add(twinFieldValueRestDTOReverseMapperV2.convert(
                            twinFieldValueRestDTOReverseMapperV2.createValueByClassIdAndFieldKey(twinEntity.getTwinClassId(), entry.getKey(), entry.getValue())));
            twinService.updateTwinFields(twinEntity, fields);
            rs.twin(twinRestDTOMapperV2.convert(
                    twinService.findTwin(apiUser, twinId), new MapperProperties()
                            .setMode(showUserMode)
                            .setMode(showStatusMode)
                            .setMode(showClassMode)
                            .setMode(showClassFieldListMode)
                            .setMode(showClassFieldMode)
                            .setMode(showTwinMode)
                            .setMode(showTwinFieldMode)
                            .setMode(showTwinAttachmentMode)
            ));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
