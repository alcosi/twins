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
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinRsDTOv2;
import org.twins.core.dto.rest.twin.TwinUpdateRqDTOv1;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.attachment.AttachmentAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.attachment.AttachmentUpdateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.*;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinUpdateController extends ApiController {
    final AuthService authService;
    final TwinService twinService;
    final TwinFieldValueRestDTOReverseMapper twinFieldValueRestDTOReverseMapper;
    final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    final UserService userService;
    final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    final AttachmentUpdateRestDTOReverseMapper attachmentUpdateRestDTOReverseMapper;
    final AttachmentAddRestDTOReverseMapper attachmentAddRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinUpdateV1", summary = "Update twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/{twinId}/v1", method = RequestMethod.PUT)
    public ResponseEntity<?> twinUpdateV1(
            @Parameter(name = "twinId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Parameter(name = "showUserMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = UserRestDTOMapper.Mode._ID_ONLY) UserRestDTOMapper.Mode showUserMode,
            @Parameter(name = "showStatusMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinStatusRestDTOMapper.Mode._ID_ONLY) TwinStatusRestDTOMapper.Mode showStatusMode,
            @Parameter(name = "showClassMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.ClassMode._ID_ONLY) TwinClassRestDTOMapper.ClassMode showClassMode,
            @Parameter(name = "showClassFieldListMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.FieldsMode._NO_FIELDS) TwinClassRestDTOMapper.FieldsMode showClassFieldListMode,
            @Parameter(name = "showClassFieldMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassFieldRestDTOMapper.Mode._ID_KEY_ONLY) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @Parameter(name = "showTwinMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.TwinMode._DETAILED) TwinRestDTOMapper.TwinMode showTwinMode,
            @Parameter(name = "showTwinFieldsMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @Parameter(name = "showTwinAttachmentMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.AttachmentsMode._HIDE) TwinRestDTOMapper.AttachmentsMode showTwinAttachmentMode,
            @RequestBody TwinUpdateRqDTOv1 request) {
        TwinRsDTOv2 rs = new TwinRsDTOv2();
        try {
            ApiUser apiUser = authService.getApiUser();
            TwinEntity dbTwinEntity = twinService.findTwin(apiUser, twinId, EntitySmartService.FindMode.ifEmptyThrows);
            TwinEntity twinEntity = new TwinEntity()
                    .setId(twinId)
                    .setName(request.getName())
                    .setHeadTwinId(request.getHeadTwinId())
                    .setAssignerUserId(userService.checkUserId(request.getAssignerUserId(), EntitySmartService.CheckMode.EMPTY_OR_DB_EXISTS))
                    .setDescription(request.getDescription());
            List<FieldValue> fields = new ArrayList<>();
            if (request.getFields() != null)
                for (Map.Entry<String, String> entry : request.getFields().entrySet())
                    fields.add(twinFieldValueRestDTOReverseMapperV2.convert(
                            twinFieldValueRestDTOReverseMapperV2.createValueByClassIdAndFieldKey(dbTwinEntity.getTwinClassId(), entry.getKey(), entry.getValue())));
            List<TwinAttachmentEntity> attachmentAddEntityList = attachmentAddRestDTOReverseMapper
                    .convertList(request.getAttachmentsAdd());
            List<TwinAttachmentEntity> attachmentUpdateEntityList = attachmentUpdateRestDTOReverseMapper
                    .convertList(request.getAttachmentsUpdate());
            twinService.updateTwin(twinEntity, dbTwinEntity, fields, attachmentAddEntityList, request.getAttachmentsDelete(), attachmentUpdateEntityList);
            rs.twin = twinRestDTOMapperV2
                    .convert(twinService.findTwin(apiUser, twinId), new MapperProperties()
                            .setMode(showUserMode)
                            .setMode(showStatusMode)
                            .setMode(showClassMode)
                            .setMode(showClassFieldListMode)
                            .setMode(showClassFieldMode)
                            .setMode(showTwinMode)
                            .setMode(showTwinFieldMode)
                            .setMode(showTwinAttachmentMode));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
