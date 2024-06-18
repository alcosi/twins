package org.twins.core.controller.rest.priv.datalist;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListOptionMapRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionMapRsDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.service.datalist.DataListService;

import java.util.UUID;

@Tag(description = "Get data list option", name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DataListOptionController extends ApiController {
    final DataListService dataListService;
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListOptionViewV1", summary = "Returns list data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/data_list_option/{dataListOptionId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> dataListOptionV1(
            @Parameter(example = DTOExamples.DATA_LIST_OPTION_ID) @PathVariable UUID dataListOptionId,
            @RequestParam(name = RestRequestParam.showDataListOptionMode, defaultValue = MapperMode.DataListOptionMode.Fields.SHORT) MapperMode.DataListOptionMode showDataListOptionMode) {
        DataListOptionRsDTOv1 rs = new DataListOptionRsDTOv1();
        try {
            DataListOptionEntity dataListOptionEntity = dataListService.findDataListOption(dataListOptionId);
            rs
                    .dataListId(dataListOptionEntity.getDataListId())
                    .option(dataListOptionRestDTOMapper.convert(dataListOptionEntity, new MapperContext().setMode(showDataListOptionMode)));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListOptionsMapViewV1", summary = "Returns map option id ref list data option")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map {option id/list data option} prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionMapRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/data_list_option/map/v1", method = RequestMethod.POST)
    public ResponseEntity<?> dataListsOptionsMapV1(
            @RequestParam(name = RestRequestParam.showDataListOptionMode, defaultValue = MapperMode.DataListOptionMode.Fields.SHORT) MapperMode.DataListOptionMode showDataListOptionMode,
            @RequestBody DataListOptionMapRqDTOv1 request) {
        DataListOptionMapRsDTOv1 rs = new DataListOptionMapRsDTOv1();
        try {
            rs
                    .dataListOptionMap(dataListOptionRestDTOMapper.convertMap(
                            dataListService.findDataListOptionsByIds(request.dataListOptionIdSet()).getMap(),
                            new MapperContext().setMode(showDataListOptionMode)
                    ));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }


}
