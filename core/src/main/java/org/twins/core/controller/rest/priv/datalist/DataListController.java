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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.*;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;

import java.util.List;
import java.util.UUID;

import static org.cambium.common.util.PaginationUtils.*;

@Tag(description = "Get data lists", name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DataListController extends ApiController {
    private final AuthService authService;
    private final DataListService dataListService;
    private final DataListRestDTOMapper dataListRestDTOMapper;
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListViewV1", summary = "Returns list data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/data_list/{dataListId}/v1", method = RequestMethod.GET)
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> dataListViewV1(
            @Parameter(example = DTOExamples.DATA_LIST_ID) @PathVariable UUID dataListId,
            @RequestParam(name = RestRequestParam.showDataListMode, defaultValue = DataListRestDTOMapper.Mode._DETAILED) DataListRestDTOMapper.Mode showDataListMode,
            @RequestParam(name = RestRequestParam.showDataListOptionMode, defaultValue = DataListOptionRestDTOMapper.Mode._DETAILED) DataListOptionRestDTOMapper.Mode showDataListOptionMode,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit) {
        DataListRsDTOv1 rs = new DataListRsDTOv1();
        DataListResult dataListResult;
        try {
            MapperContext mapperContext = new MapperContext()
                    .setMode(showDataListMode)
                    .setMode(showDataListOptionMode, createSimplePagination(offset, limit, Sort.unsorted()));
            DataListEntity dataListEntity = dataListService.findEntitySafe(dataListId);
            DataListDTOv1 dataListDTO = dataListRestDTOMapper.convert(dataListEntity, mapperContext);
            if (!dataListOptionRestDTOMapper.hideMode(mapperContext)) {
                dataListResult = dataListService.getDataList(dataListEntity, dataListDTO, mapperContext);
            } else {
                dataListResult = (DataListResult) new DataListResult()
                        .setDataList(dataListDTO)
                        .setTotal(0)
                        .setOffset(offset)
                        .setLimit(limit);
            }
            rs
                    .setDataList(dataListResult.getDataList())
                    .setPagination(paginationMapper.convert(dataListResult));
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
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> dataListByKeyViewV1(
            @Parameter(example = DTOExamples.DATA_LIST_KEY) @PathVariable String dataListKey,
            @RequestParam(name = RestRequestParam.showDataListMode, defaultValue = DataListRestDTOMapper.Mode._DETAILED) DataListRestDTOMapper.Mode showDataListMode,
            @RequestParam(name = RestRequestParam.showDataListOptionMode, defaultValue = DataListOptionRestDTOMapper.Mode._DETAILED) DataListOptionRestDTOMapper.Mode showDataListOptionMode,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit) {
        DataListRsDTOv1 rs = new DataListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.dataList = dataListRestDTOMapper.convert(
                    dataListService.findDataListByKey(apiUser, dataListKey), new MapperContext()
                            .setMode(showDataListMode)
                            .setMode(showDataListOptionMode, createSimplePagination(offset, limit, Sort.unsorted()))); //todo need implementation
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
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> dataListSearchV1(
            @RequestParam(name = RestRequestParam.showDataListMode, defaultValue = DataListRestDTOMapper.Mode._DETAILED) DataListRestDTOMapper.Mode showDataListMode,
            @RequestParam(name = RestRequestParam.showDataListOptionMode, defaultValue = DataListOptionRestDTOMapper.Mode._HIDE) DataListOptionRestDTOMapper.Mode showDataListOptionMode,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit,
            @RequestBody DataListSearchRqDTOv1 request) {
        DataListSearchRsDTOv1 rs = new DataListSearchRsDTOv1();
        try {
            List<DataListDTOv1> dataListDTOv1s = dataListRestDTOMapper.convertList(
                    dataListService.findDataLists(request.dataListIdList()), new MapperContext()
                            .setMode(showDataListMode)
                            .setMode(showDataListOptionMode, createSimplePagination(offset, limit, Sort.unsorted())));
            rs.dataListList(); //todo need implementation
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
