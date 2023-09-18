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
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinRsDTOv1;
import org.twins.core.dto.rest.twin.TwinRsDTOv2;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

@Tag(description = "", name = "twin")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinViewController extends ApiController {
    private final AuthService authService;
    private final TwinService twinService;
    private final TwinRestDTOMapper twinRestDTOMapper;
    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinViewV1", summary = "Returns twin data by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/{twinId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinViewV1(
            @Parameter(name = "twinId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Parameter(name = "showUserMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = UserDTOMapper.Mode._ID_ONLY) UserDTOMapper.Mode showUserMode,
            @Parameter(name = "showStatusMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinStatusRestDTOMapper.Mode._ID_ONLY) TwinStatusRestDTOMapper.Mode showStatusMode,
            @Parameter(name = "showClassMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.ClassMode._ID_ONLY) TwinClassRestDTOMapper.ClassMode showClassMode,
            @Parameter(name = "showTwinMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.TwinMode._DETAILED) TwinRestDTOMapper.TwinMode showTwinMode,
            @Parameter(name = "showTwinFieldsMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @Parameter(name = "showTwinClassFieldMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassFieldRestDTOMapper.Mode._ID_KEY_ONLY) TwinClassFieldRestDTOMapper.Mode showTwinClassFieldMode,
            @Parameter(name = "showTwinAttachmentMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.AttachmentsMode._HIDE) TwinRestDTOMapper.AttachmentsMode showTwinAttachmentMode) {
        TwinRsDTOv1 rs = new TwinRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.twin(twinRestDTOMapper.convert(
                    twinService.findTwin(apiUser, twinId), new MapperProperties()
                            .setMode(showUserMode)
                            .setMode(showStatusMode)
                            .setMode(showClassMode)
                            .setMode(showTwinMode)
                            .setMode(showTwinFieldMode)
                            .setMode(showTwinClassFieldMode)
                            .setMode(showTwinAttachmentMode)
            ));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinViewV2", summary = "Returns twin data by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/{twinId}/v2", method = RequestMethod.GET)
    public ResponseEntity<?> twinViewV2(
            @Parameter(name = "twinId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Parameter(name = "showUserMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = UserDTOMapper.Mode._ID_ONLY) UserDTOMapper.Mode showUserMode,
            @Parameter(name = "showStatusMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinStatusRestDTOMapper.Mode._ID_ONLY) TwinStatusRestDTOMapper.Mode showStatusMode,
            @Parameter(name = "showClassMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.ClassMode._ID_ONLY) TwinClassRestDTOMapper.ClassMode showClassMode,
            @Parameter(name = "showClassFieldListMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.FieldsMode._NO_FIELDS) TwinClassRestDTOMapper.FieldsMode showClassFieldListMode,
            @Parameter(name = "showClassFieldMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassFieldRestDTOMapper.Mode._ID_KEY_ONLY) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @Parameter(name = "showTwinMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.TwinMode._DETAILED) TwinRestDTOMapper.TwinMode showTwinMode,
            @Parameter(name = "showTwinFieldsMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @Parameter(name = "showTwinAttachmentMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.AttachmentsMode._HIDE) TwinRestDTOMapper.AttachmentsMode showTwinAttachmentMode) {
        TwinRsDTOv2 rs = new TwinRsDTOv2();
        try {
            ApiUser apiUser = authService.getApiUser();
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

    @ParametersApiUserHeaders
    @Operation(operationId = "twinViewByAliasV1", summary = "Returns twin data by alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_by_alias/{twinAlias}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinViewByAliasV1(
            @Parameter(name = "twinAlias", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_ID) @PathVariable String twinAlias,
            @Parameter(name = "showUserMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = UserDTOMapper.Mode._ID_ONLY) UserDTOMapper.Mode showUserMode,
            @Parameter(name = "showStatusMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinStatusRestDTOMapper.Mode._ID_ONLY) TwinStatusRestDTOMapper.Mode showStatusMode,
            @Parameter(name = "showClassMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.ClassMode._ID_ONLY) TwinClassRestDTOMapper.ClassMode showClassMode,
            @Parameter(name = "showTwinMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.TwinMode._DETAILED) TwinRestDTOMapper.TwinMode showTwinMode,
            @Parameter(name = "showTwinFieldsMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @Parameter(name = "showTwinClassFieldMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassFieldRestDTOMapper.Mode._ID_KEY_ONLY) TwinClassFieldRestDTOMapper.Mode showTwinClassFieldMode,
            @Parameter(name = "showTwinAttachmentMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.AttachmentsMode._HIDE) TwinRestDTOMapper.AttachmentsMode showTwinAttachmentMode) {
        TwinRsDTOv1 rs = new TwinRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.twin(twinRestDTOMapper.convert(
                    twinService.findTwinByAlias(apiUser, twinAlias), new MapperProperties()
                            .setMode(showUserMode)
                            .setMode(showStatusMode)
                            .setMode(showClassMode)
                            .setMode(showTwinMode)
                            .setMode(showTwinFieldMode)
                            .setMode(showTwinClassFieldMode)
                            .setMode(showTwinAttachmentMode)
            ));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinViewByAliasV2", summary = "Returns twin data by alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_by_alias/{twinAlias}/v2", method = RequestMethod.GET)
    public ResponseEntity<?> twinViewByAliasV2(
            @Parameter(name = "twinAlias", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_ID) @PathVariable String twinAlias,
            @Parameter(name = "showUserMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = UserDTOMapper.Mode._ID_ONLY) UserDTOMapper.Mode showUserMode,
            @Parameter(name = "showStatusMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinStatusRestDTOMapper.Mode._ID_ONLY) TwinStatusRestDTOMapper.Mode showStatusMode,
            @Parameter(name = "showClassMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.ClassMode._ID_ONLY) TwinClassRestDTOMapper.ClassMode showClassMode,
            @Parameter(name = "showClassFieldListMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.FieldsMode._NO_FIELDS) TwinClassRestDTOMapper.FieldsMode showClassFieldListMode,
            @Parameter(name = "showClassFieldMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassFieldRestDTOMapper.Mode._ID_KEY_ONLY) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @Parameter(name = "showTwinMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.TwinMode._DETAILED) TwinRestDTOMapper.TwinMode showTwinMode,
            @Parameter(name = "showTwinFieldsMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @Parameter(name = "showTwinAttachmentMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.AttachmentsMode._HIDE) TwinRestDTOMapper.AttachmentsMode showTwinAttachmentMode) {
        TwinRsDTOv1 rs = new TwinRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.twin(twinRestDTOMapper.convert(
                    twinService.findTwinByAlias(apiUser, twinAlias), new MapperProperties()
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
