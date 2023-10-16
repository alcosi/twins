package org.twins.core.controller.rest.priv.twinclass;

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
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.twinclass.TwinClassSearchRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassSearchRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinClassListController extends ApiController {
    private final AuthService authService;
    private final TwinClassService twinClassService;
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassSearchV1", summary = "Returns twin class search result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassSearchRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_class/search/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinClassSearchV1(
            @Parameter(name = "showTwinClassMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassBaseRestDTOMapper.ClassMode._ID_ONLY) TwinClassBaseRestDTOMapper.ClassMode showTwinClassMode,
            @Parameter(name = "showTwinClassFieldMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.FieldsMode._ALL_FIELDS) TwinClassRestDTOMapper.FieldsMode showTwinClassFieldMode,
            @Parameter(name = "showTwinClassFieldDescriptorMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassFieldRestDTOMapper.Mode._DETAILED) TwinClassFieldRestDTOMapper.Mode showTwinClassFieldDescriptorMode,
            @Parameter(name = "showTwinClassHeadsMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.HeadTwinMode._SHOW) TwinClassRestDTOMapper.HeadTwinMode showTwinClassHeadsMode,
            @Parameter(name = "showTwinClassLinksMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.LinksMode._SHOW) TwinClassRestDTOMapper.LinksMode showTwinClassLinksMode,
            @RequestBody TwinClassSearchRqDTOv1 request) {
        TwinClassSearchRsDTOv1 rs = new TwinClassSearchRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.twinClassList(
                    twinClassRestDTOMapper.convertList(
                            twinClassService.findTwinClasses(apiUser, request.twinClassIdList()), new MapperContext()
                                    .setMode(showTwinClassMode)
                                    .setMode(showTwinClassHeadsMode)
                                    .setMode(showTwinClassFieldDescriptorMode)
                                    .setMode(showTwinClassFieldMode)
                                    .setMode(showTwinClassLinksMode)));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassListV1", summary = "Returns twin class list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassSearchRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_class/list/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinClassListV1(
            @Parameter(name = "showTwinClassMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassBaseRestDTOMapper.ClassMode._ID_ONLY) TwinClassBaseRestDTOMapper.ClassMode showTwinClassMode,
            @Parameter(name = "showTwinClassFieldMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.FieldsMode._ALL_FIELDS) TwinClassRestDTOMapper.FieldsMode showTwinClassFieldMode,
            @Parameter(name = "showTwinClassFieldDescriptorMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassFieldRestDTOMapper.Mode._DETAILED) TwinClassFieldRestDTOMapper.Mode showTwinClassFieldDescriptorMode,
            @Parameter(name = "showTwinClassHeadsMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.HeadTwinMode._SHOW) TwinClassRestDTOMapper.HeadTwinMode showTwinClassHeadsMode,
            @Parameter(name = "showTwinClassLinksMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = TwinClassRestDTOMapper.LinksMode._SHOW) TwinClassRestDTOMapper.LinksMode showTwinClassLinksMode) {
        TwinClassSearchRsDTOv1 rs = new TwinClassSearchRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.twinClassList(
                    twinClassRestDTOMapper.convertList(
                            twinClassService.findTwinClasses(apiUser, null), new MapperContext()
                                    .setMode(showTwinClassMode)
                                    .setMode(showTwinClassHeadsMode)
                                    .setMode(showTwinClassFieldDescriptorMode)
                                    .setMode(showTwinClassFieldMode)
                                    .setMode(TwinRestDTOMapper.FieldsMode.NO_FIELDS)
                                    .setMode(showTwinClassLinksMode)));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
