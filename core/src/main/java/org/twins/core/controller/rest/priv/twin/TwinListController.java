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
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.PaginationBean;
import org.twins.core.dto.rest.twin.TwinSearchRqDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRsDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRsDTOv2;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twin.TwinSearchRqDTOMapper;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.List;

@Tag(description = "", name = "twin")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinListController extends ApiController {
    final AuthService authService;
    final TwinService twinService;
    final TwinRestDTOMapper twinRestDTOMapper;
    final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    final TwinSearchRqDTOMapper twinSearchRqDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinSearchV1", summary = "Returns twin list by tql")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/search/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinSearchV1(
            @Parameter(name = "showUserMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = UserDTOMapper.Mode._ID_ONLY) UserDTOMapper.Mode showUserMode,
            @Parameter(name = "showStatusMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinStatusRestDTOMapper.Mode._ID_ONLY) TwinStatusRestDTOMapper.Mode showStatusMode,
            @Parameter(name = "showClassMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.ClassMode._ID_ONLY) TwinClassRestDTOMapper.ClassMode showClassMode,
            @Parameter(name = "showTwinMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.TwinMode._DETAILED) TwinRestDTOMapper.TwinMode showTwinMode,
            @Parameter(name = "showTwinFieldsMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @RequestBody TwinSearchRqDTOv1 request) {
        TwinSearchRsDTOv1 rs = new TwinSearchRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            List<TwinEntity> twinList = twinService.findTwins(apiUser, twinSearchRqDTOMapper.convert(request));
            rs
                    .setTwinList(twinRestDTOMapper.convertList(
                            twinList, new MapperProperties()
                                    .setMode(showUserMode)
                                    .setMode(showStatusMode)
                                    .setMode(showClassMode)
                                    .setMode(showTwinMode)
                                    .setMode(showTwinFieldMode)                    ));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinSearchV2", summary = "Returns twin list by tql")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/search/v2", method = RequestMethod.POST)
    public ResponseEntity<?> twinSearchV2(
            @Parameter(name = "showUserMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = UserDTOMapper.Mode._ID_ONLY) UserDTOMapper.Mode showUserMode,
            @Parameter(name = "showStatusMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinStatusRestDTOMapper.Mode._ID_ONLY) TwinStatusRestDTOMapper.Mode showStatusMode,
            @Parameter(name = "showClassMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.ClassMode._ID_ONLY) TwinClassRestDTOMapper.ClassMode showClassMode,
            @Parameter(name = "showClassFieldListMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.FieldsMode._NO_FIELDS) TwinClassRestDTOMapper.FieldsMode showClassFieldListMode,
            @Parameter(name = "showClassFieldMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassFieldRestDTOMapper.Mode._ID_KEY_ONLY) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @Parameter(name = "showTwinMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.TwinMode._DETAILED) TwinRestDTOMapper.TwinMode showTwinMode,
            @Parameter(name = "showTwinFieldsMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.FieldsMode._ALL_FIELDS) TwinRestDTOMapper.FieldsMode showTwinFieldMode,
            @Parameter(name = "showTwinAttachmentMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.AttachmentsMode._HIDE) TwinRestDTOMapper.AttachmentsMode showTwinAttachmentMode,
            @RequestBody TwinSearchRqDTOv1 request) {
        TwinSearchRsDTOv2 rs = new TwinSearchRsDTOv2();
        try {
            ApiUser apiUser = authService.getApiUser();
            List<TwinEntity> twinList = twinService.findTwins(apiUser, twinSearchRqDTOMapper.convert(request));
            rs
                    .setTwinList(twinRestDTOMapperV2.convertList(
                            twinList, new MapperProperties()
                                    .setMode(showUserMode)
                                    .setMode(showStatusMode)
                                    .setMode(showClassMode)
                                    .setMode(showClassFieldListMode)
                                    .setMode(showClassFieldMode)
                                    .setMode(showTwinMode)
                                    .setMode(showTwinFieldMode)
                                    .setMode(showTwinAttachmentMode)));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
