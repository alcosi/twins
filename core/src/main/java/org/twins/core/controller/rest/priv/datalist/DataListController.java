package org.twins.core.controller.rest.priv.datalist;

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
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListRsDTOv1;
import org.twins.core.dto.rest.datalist.DataListSearchRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListSearchRsDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;

import java.util.UUID;

@Tag(description = "Get data lists", name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DataListController extends ApiController {
    private final AuthService authService;
    private final DataListService dataListService;
    private final DataListRestDTOMapper dataListRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListViewV1", summary = "Returns list data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/data_list/{dataListId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> dataListViewV1(
            @Parameter(name = "dataListId", in = ParameterIn.PATH, required = true, example = DTOExamples.DATA_LIST_ID) @PathVariable UUID dataListId,
            @Parameter(name = "showDataListMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = DataListRestDTOMapper.Mode._SHOW_OPTIONS) DataListRestDTOMapper.Mode showDataListMode,
            @Parameter(name = "showDataListOptionMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = DataListOptionRestDTOMapper.Mode._DETAILED) DataListOptionRestDTOMapper.Mode showDataListOptionMode) {
        DataListRsDTOv1 rs = new DataListRsDTOv1();
        try {
            rs.dataList = dataListRestDTOMapper.convert(
                    dataListService.findEntitySafe(dataListId), new MapperProperties()
                            .setMode(showDataListMode)
                            .setMode(showDataListOptionMode));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListByKeyViewV1", summary = "Returns list data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/data_list_by_key/{dataListKey}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> dataListByKeyViewV1(
            @Parameter(name = "dataListKey", in = ParameterIn.PATH, required = true, example = DTOExamples.DATA_LIST_KEY) @PathVariable String dataListKey,
            @Parameter(name = "showDataListMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = DataListRestDTOMapper.Mode._SHOW_OPTIONS) DataListRestDTOMapper.Mode showDataListMode,
            @Parameter(name = "showDataListOptionMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = DataListOptionRestDTOMapper.Mode._DETAILED) DataListOptionRestDTOMapper.Mode showDataListOptionMode) {
        DataListRsDTOv1 rs = new DataListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.dataList = dataListRestDTOMapper.convert(
                    dataListService.findDataListByKey(apiUser, dataListKey), new MapperProperties()
                            .setMode(showDataListMode)
                            .setMode(showDataListOptionMode));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListSearchV1", summary = "Returns lists details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListSearchRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/data_list/search/v1", method = RequestMethod.POST)
    public ResponseEntity<?> dataListSearchV1(
            @Parameter(name = "showDataListMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = DataListRestDTOMapper.Mode._SHOW_OPTIONS) DataListRestDTOMapper.Mode showDataListMode,
            @Parameter(name = "showDataListOptionMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = DataListOptionRestDTOMapper.Mode._DETAILED) DataListOptionRestDTOMapper.Mode showDataListOptionMode,
            @RequestBody DataListSearchRqDTOv1 request) {
        DataListSearchRsDTOv1 rs = new DataListSearchRsDTOv1();
        try {
            rs.dataListList(
                    dataListRestDTOMapper.convertList(
                            dataListService.findDataLists(request.dataListIdList()), new MapperProperties()
                                    .setMode(showDataListMode)
                                    .setMode(showDataListOptionMode)));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
