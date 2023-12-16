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
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinCreate;
import org.twins.core.dto.rest.twin.TwinCreateRqDTOv1;
import org.twins.core.dto.rest.twin.TwinCreateRqDTOv2;
import org.twins.core.dto.rest.twin.TwinCreateRsDTOv1;
import org.twins.core.dto.rest.twin.TwinFieldValueDTO;
import org.twins.core.mappers.rest.attachment.AttachmentAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.TwinLinkAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinCreateRqRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinCreateRsRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapper;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinCreateController extends ApiController {
    final AuthService authService;
    final TwinService twinService;
    final TwinFieldValueRestDTOReverseMapper twinFieldValueRestDTOReverseMapper;
    final UserService userService;
    final TwinCreateRsRestDTOMapper twinCreateRsRestDTOMapper;
    final AttachmentAddRestDTOReverseMapper attachmentAddRestDTOReverseMapper;
    final TwinLinkAddRestDTOReverseMapper twinLinkAddRestDTOReverseMapper;
    final TwinCreateRqRestDTOReverseMapper twinCreateRqRestDTOReverseMapper;

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
            List<TwinFieldValueDTO> fields = new ArrayList<>();
            if (request.getFields() != null)
                for (Map.Entry<String, TwinFieldValueDTO> entry : request.getFields().entrySet())
                    fields.add(entry.getValue()
                            .fieldKey(entry.getKey())
                            .twinClassId(request.getClassId()));
            TwinCreate twinCreate = new TwinCreate();
            twinCreate
                    .setTwinEntity(new TwinEntity()
                            .setTwinClassId(request.getClassId())
                            .setName(request.getName())
                            .setCreatedByUserId(apiUser.getUser().getId())
                            .setHeadTwinId(request.getHeadTwinId())
                            .setAssignerUserId(userService.checkId(request.getAssignerUserId(), EntitySmartService.CheckMode.EMPTY_OR_DB_EXISTS))
                            .setDescription(request.getDescription()))
                    .setFields(twinFieldValueRestDTOReverseMapper.convertList(fields));
            twinCreate
                    .setAttachmentEntityList(attachmentAddRestDTOReverseMapper.convertList(request.getAttachments()))
                    .setLinksEntityList(twinLinkAddRestDTOReverseMapper.convertList(request.getLinks()));
            rs = twinCreateRsRestDTOMapper
                    .convert(twinService
                            .createTwin(apiUser, twinCreate));
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
            TwinCreate twinCreate = twinCreateRqRestDTOReverseMapper.convert(request);
            rs = twinCreateRsRestDTOMapper
                    .convert(twinService
                            .createTwin(apiUser, twinCreate));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
