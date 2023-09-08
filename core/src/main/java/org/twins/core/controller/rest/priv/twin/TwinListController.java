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
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinRsDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRqDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRsDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.List;
import java.util.UUID;

@Tag(description = "", name = "twin")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinListController extends ApiController {
    private final AuthService authService;
    private final TwinService twinService;
    private final TwinRestDTOMapper twinRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinViewV1", summary = "Returns twin data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/{twinId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinListV1(
            @Parameter(name = "twinId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Parameter(name = "showUserMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = UserDTOMapper.Mode._ID_ONLY) UserDTOMapper.Mode showUserMode,
            @Parameter(name = "showStatusMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinStatusRestDTOMapper.Mode._ID_ONLY) TwinStatusRestDTOMapper.Mode showStatusMode,
            @Parameter(name = "showClassMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.Mode._ID_ONLY) TwinClassRestDTOMapper.Mode showClassMode,
            @Parameter(name = "showTwinMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.Mode._FIELDS_VALUES) TwinRestDTOMapper.Mode showTwinMode,
            @Parameter(name = "showTwinClassFieldMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassFieldRestDTOMapper.Mode._ID_KEY_ONLY) TwinClassFieldRestDTOMapper.Mode showTwinClassFieldMode) {
        TwinRsDTOv1 rs = new TwinRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.twin(twinRestDTOMapper.convert(
                    twinService.findTwin(apiUser, twinId), new MapperProperties()
                            .setMode(showUserMode)
                            .setMode(showStatusMode)
                            .setMode(showClassMode)
                            .setMode(showTwinMode)
                            .setMode(showTwinClassFieldMode)
                    ));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

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
            @Parameter(name = "showClassMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.Mode._ID_ONLY) TwinClassRestDTOMapper.Mode showClassMode,
            @Parameter(name = "showTwinMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinRestDTOMapper.Mode._FIELDS_VALUES) TwinRestDTOMapper.Mode showTwinMode,
            @Parameter(name = "showTwinClassFieldMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassFieldRestDTOMapper.Mode._ID_KEY_ONLY) TwinClassFieldRestDTOMapper.Mode showTwinClassFieldMode,
            @RequestBody TwinSearchRqDTOv1 request) {
        TwinSearchRsDTOv1 rs = new TwinSearchRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            List<TwinEntity> twinList = twinService.findTwins(apiUser, null);
            rs.twinList(twinRestDTOMapper.convertList(
                    twinList, new MapperProperties()
                            .setMode(showUserMode)
                            .setMode(showStatusMode)
                            .setMode(showClassMode)
                            .setMode(showTwinMode)
                            .setMode(showTwinClassFieldMode)
            ));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
