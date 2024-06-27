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
import org.cambium.service.EntitySmartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.MapperModeParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinFieldListUpdateRqDTOv1;
import org.twins.core.dto.rest.twin.TwinFieldRsDTOv1;
import org.twins.core.dto.rest.twin.TwinFieldUpdateRqDTOv1;
import org.twins.core.dto.rest.twin.TwinRsDTOv2;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperModePointer;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.*;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.List;
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
    final TwinFieldRestDTOMapper twinFieldRestDTOMapper;
    final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;


    @ParametersApiUserHeaders
    @Operation(operationId = "twinFieldByKeySaveV1", summary = "Creates or updates twin field data by key. ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinFieldRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/{twinId}/field/{fieldKey}/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinFieldSaveV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Parameter(example = DTOExamples.TWIN_FIELD_KEY) @PathVariable String fieldKey,
            @RequestBody TwinFieldUpdateRqDTOv1 request) {
        TwinFieldRsDTOv1 rs = new TwinFieldRsDTOv1();
        try {
            TwinField twinField = twinService.wrapField(twinId, fieldKey);
            twinService.updateField(twinField, twinFieldValueRestDTOReverseMapper.convert(request.value));
            rs.field(twinFieldRestDTOMapper.convert(twinField));
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
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Parameter(example = DTOExamples.TWIN_FIELD_KEY) @PathVariable String fieldKey,
            @Parameter(example = DTOExamples.TWIN_FIELD_VALUE) @RequestParam(name = RestRequestParam.fieldValue) String fieldValue) {
        TwinFieldRsDTOv1 rs = new TwinFieldRsDTOv1();
        try {
            TwinField twinField = twinService.wrapField(twinId, fieldKey);
            twinService.updateField(twinField, twinFieldValueRestDTOReverseMapperV2.convert(
                    twinFieldValueRestDTOReverseMapperV2.createByTwinIdAndFieldKey(twinId, fieldKey, fieldValue)
            ));
            rs.field(twinFieldRestDTOMapper.convert(twinField));
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
    public ResponseEntity<?> twinFieldListUpdateV1(MapperContext mapperContext,
            @Parameter(name = "twinId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._SHORT) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = MapperMode.StatusMode.Fields.SHORT) MapperModePointer.StatusMode showStatusMode,
            @RequestParam(name = RestRequestParam.showClassMode, defaultValue = TwinClassBaseRestDTOMapper.ClassMode._SHORT) TwinClassBaseRestDTOMapper.ClassMode showClassMode,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @MapperModeParam MapperMode.TwinDefaultMode showTwinMode,
            @RequestParam(name = RestRequestParam.showTwinFieldMode, defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @RequestParam(name = RestRequestParam.showAttachmentMode, defaultValue = MapperMode.AttachmentMode.Fields.HIDE) MapperMode.AttachmentMode showAttachmentMode,
            @RequestBody TwinFieldListUpdateRqDTOv1 request) {
        TwinRsDTOv2 rs = new TwinRsDTOv2();
        try {
            TwinEntity twinEntity = twinService.findEntity(twinId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
            List<FieldValue> fields = twinFieldValueRestDTOReverseMapperV2.mapFields(twinEntity.getTwinClassId(), request.getFields());
            twinService.updateTwinFields(twinEntity, fields);
            rs
                    .twin(twinRestDTOMapperV2.convert(twinService.findEntitySafe(twinId), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
