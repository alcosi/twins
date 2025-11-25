package org.twins.core.controller.rest.pub.datalist;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserAnonymousHeaders;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListRsDTOv1;
import org.twins.core.dto.rest.datalist.DataListSearchRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListSearchRsDTOv1;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListSearchService;
import org.twins.core.service.datalist.DataListService;

import java.util.UUID;

@Tag(description = "Get public data lists", name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DataListPublicController extends ApiController {
    private final AuthService authService;
    private final DataListService dataListService;
    private final DataListRestDTOMapper dataListRestDTOMapperV2;
    private final DataListSearchDTOReverseMapper dataListSearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final DataListSearchService dataListSearchService;

    @ParametersApiUserAnonymousHeaders
    @Operation(operationId = "dataListPublicViewV1", summary = "Returns public data list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Public list details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/public/data_list/{dataListId}/v1")
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> dataListPublicViewV1(
            @MapperContextBinding(roots = DataListRestDTOMapper.class, response = DataListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.DATA_LIST_ID) @PathVariable UUID dataListId) {
        DataListRsDTOv1 rs = new DataListRsDTOv1();
        try {
            authService.getApiUser().setAnonymous();
            rs.dataList = dataListRestDTOMapperV2.convert(
                    dataListService.findEntitySafe(dataListId), mapperContext);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserAnonymousHeaders
    @Operation(operationId = "dataListPublicByKeyViewV1", summary = "Returns public data list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Public list details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/public/data_list_by_key/{dataListKey}/v1")
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> dataListPublicByKeyViewV1(
            @MapperContextBinding(roots = DataListRestDTOMapper.class, response = DataListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.DATA_LIST_KEY) @PathVariable String dataListKey) {
        DataListRsDTOv1 rs = new DataListRsDTOv1();
        try {
            authService.getApiUser().setAnonymous();
            rs
                    .setDataList(dataListRestDTOMapperV2.convert(
                            dataListService.findEntitySafe(dataListKey), mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserAnonymousHeaders
    @Operation(operationId = "dataListPublicSearchV1", summary = "Returns public details lists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Public list details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListSearchRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/public/data_list/search/v1")
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> dataListPublicSearchV1(
            @MapperContextBinding(roots = DataListRestDTOMapper.class, response = DataListSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody DataListSearchRqDTOv1 request) {
        DataListSearchRsDTOv1 rs = new DataListSearchRsDTOv1();
        try {
            authService.getApiUser().setAnonymous();
            PaginationResult<DataListEntity> dataListsList = dataListSearchService.findDataListsForDomain(dataListSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setDataListList(dataListRestDTOMapperV2.convertCollection(dataListsList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(dataListsList));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
