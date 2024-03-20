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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.ParametersApiUserAnonymousHeaders;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.BusinessAccountResolverNotSpecified;
import org.twins.core.domain.apiuser.DomainResolverGivenId;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListRsDTOv1;
import org.twins.core.dto.rest.datalist.DataListSearchRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListSearchRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;

import java.util.UUID;

@Tag(description = "Get data lists", name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DataPublicListController extends ApiController {
    private final AuthService authService;
    private final DataListService dataListService;
    private final DataListRestDTOMapper dataListRestDTOMapper;

    @ParametersApiUserAnonymousHeaders
    @Operation(operationId = "dataListViewV1", summary = "Returns public list data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/public/data_list/v1", method = RequestMethod.GET)
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> dataListViewV1(
            @RequestParam(name = RestRequestParam.showDataListMode, defaultValue = DataListRestDTOMapper.Mode._DETAILED) DataListRestDTOMapper.Mode showDataListMode,
            @RequestParam(name = RestRequestParam.showDataListOptionMode, defaultValue = DataListOptionRestDTOMapper.Mode._DETAILED) DataListOptionRestDTOMapper.Mode showDataListOptionMode) {
        DataListRsDTOv1 rs = new DataListRsDTOv1();
        try {
//            authService.getApiUser().getAnonymous();
            authService.getApiUser()
                    .setDomainResolver(new DomainResolverGivenId(UUID.fromString("f67ad556-dd27-4871-9a00-16fb0e8a4102")))
                    .setBusinessAccountResolver(new BusinessAccountResolverNotSpecified());

            rs.dataList = dataListRestDTOMapper.convert(
                    dataListService.findEntitySafe(UUID.fromString("e844a4e5-1c09-474e-816f-05cdb1f093ed")), new MapperContext()
                            .setMode(showDataListMode)
                            .setMode(showDataListOptionMode));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserAnonymousHeaders
    @Operation(operationId = "dataListByKeyViewV1", summary = "Returns public list data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/public/data_list_by_key/{dataListKey}/v1", method = RequestMethod.GET)
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> dataListByKeyViewV1(
            @Parameter(example = DTOExamples.DATA_LIST_KEY) @PathVariable String dataListKey,
            @RequestParam(name = RestRequestParam.showDataListMode, defaultValue = DataListRestDTOMapper.Mode._DETAILED) DataListRestDTOMapper.Mode showDataListMode,
            @RequestParam(name = RestRequestParam.showDataListOptionMode, defaultValue = DataListOptionRestDTOMapper.Mode._DETAILED) DataListOptionRestDTOMapper.Mode showDataListOptionMode) {
        DataListRsDTOv1 rs = new DataListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            apiUser
                    .setDomainResolver(new DomainResolverGivenId(UUID.fromString("f67ad556-dd27-4871-9a00-16fb0e8a4102")))
                    .setBusinessAccountResolver(new BusinessAccountResolverNotSpecified());
            rs.dataList = dataListRestDTOMapper.convert(
                    dataListService.findDataListByKey(apiUser, dataListKey), new MapperContext()
                            .setMode(showDataListMode)
                            .setMode(showDataListOptionMode));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserAnonymousHeaders
    @Operation(operationId = "dataListSearchV1", summary = "Returns public lists details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListSearchRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/public/data_list/search/v1", method = RequestMethod.POST)
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> dataListSearchV1(
            @RequestParam(name = RestRequestParam.showDataListMode, defaultValue = DataListRestDTOMapper.Mode._DETAILED) DataListRestDTOMapper.Mode showDataListMode,
            @RequestParam(name = RestRequestParam.showDataListOptionMode, defaultValue = DataListOptionRestDTOMapper.Mode._HIDE) DataListOptionRestDTOMapper.Mode showDataListOptionMode,
            @RequestBody DataListSearchRqDTOv1 request) {
        DataListSearchRsDTOv1 rs = new DataListSearchRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            apiUser
                    .setDomainResolver(new DomainResolverGivenId(UUID.fromString("f67ad556-dd27-4871-9a00-16fb0e8a4102")))
                    .setBusinessAccountResolver(new BusinessAccountResolverNotSpecified());
            rs.dataListList(
                    dataListRestDTOMapper.convertList(
                            dataListService.findDataLists(request.dataListIdList()), new MapperContext()
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
