package org.twins.core.controller.rest.priv.twin;

import io.swagger.v3.oas.annotations.Operation;
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
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.mappers.rest.attachment.AttachmentRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinCreateRsRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapperV2;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(description = "", name = "twin")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinCreateController extends ApiController {
    final AuthService authService;
    final TwinService twinService;
    final TwinFieldValueRestDTOReverseMapper twinFieldValueRestDTOReverseMapper;
    final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    final UserService userService;
    final TwinCreateRsRestDTOMapper twinCreateRsRestDTOMapper;
    final AttachmentRestDTOReverseMapper attachmentRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinCreateV1", summary = "Create new twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinCreateV1(
            @RequestBody TwinCreateRqDTOv1 request) {
        TwinCreateRsDTOv1 rs = new TwinCreateRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            TwinEntity twinEntity = new TwinEntity()
                    .twinClassId(request.getClassId())
                    .name(request.getName())
                    .businessAccountId(apiUser.getBusinessAccount().getId())
                    .createdByUserId(apiUser.getUser().getId())
                    .headTwinId(request.getHeadTwinId())
                    .assignerUserId(userService.checkUserId(request.getAssignerUserId(), EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS))
                    .description(request.getDescription());
            List<TwinFieldValueDTO> fields = new ArrayList<>();
            if (request.getFields() != null)
                for (Map.Entry<String, TwinFieldValueDTO> entry : request.getFields().entrySet())
                    fields.add(entry.getValue()
                            .fieldKey(entry.getKey())
                            .twinClassId(request.getClassId()));
            rs = twinCreateRsRestDTOMapper
                    .convert(twinService
                            .createTwin(twinEntity, twinFieldValueRestDTOReverseMapper
                                    .convertList(fields), attachmentRestDTOReverseMapper
                                    .convertList(request.getAttachments())));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinCreateV2", summary = "Create new twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/v2", method = RequestMethod.POST)
    public ResponseEntity<?> twinCreateV2(
            @RequestBody TwinCreateRqDTOv2 request) {
        TwinCreateRsDTOv1 rs = new TwinCreateRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            TwinEntity twinEntity = new TwinEntity()
                    .twinClassId(request.getClassId())
                    .name(request.getName())
                    .businessAccountId(apiUser.getBusinessAccount().getId())
                    .createdByUserId(apiUser.getUser().getId())
                    .headTwinId(request.getHeadTwinId())
                    .assignerUserId(userService.checkUserId(request.assignerUserId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS))
                    .description(request.getDescription());
            List<FieldValueText> fields = new ArrayList<>();
            if (request.getFields() != null)
                for (Map.Entry<String, String> entry : request.getFields().entrySet())
                    fields.add(twinFieldValueRestDTOReverseMapperV2
                            .createValueByClassIdAndFieldKey(request.getClassId(), entry.getKey(), entry.getValue()));
            rs = twinCreateRsRestDTOMapper
                    .convert(twinService
                            .createTwin(twinEntity, twinFieldValueRestDTOReverseMapperV2
                                    .convertList(fields), attachmentRestDTOReverseMapper
                                    .convertList(request.getAttachments())));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
