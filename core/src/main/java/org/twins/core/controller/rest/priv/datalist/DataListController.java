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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListRsDTOv1;
import org.twins.core.dto.rest.datalist.DataListSearchRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListSearchRsDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;

import java.util.UUID;

@Tag(description = "Get data lists", name = "dataList")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DataListController extends ApiController {
    private final AuthService authService;
    private final DataListService dataListService;
    private final DataListRestDTOMapper dataListRestDTOMapper;

    @Operation(operationId = "dataListV1", summary = "Returns list deta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/data_list/{dataListId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> dataListV1(
            @Parameter(name = "UserId", in = ParameterIn.HEADER, required = true, example = DTOExamples.USER_ID) String userId,
            @Parameter(name = "DomainId", in = ParameterIn.HEADER, required = true, example = DTOExamples.DOMAIN_ID) String domainId,
            @Parameter(name = "BusinessAccountId", in = ParameterIn.HEADER, required = true, example = DTOExamples.BUSINESS_ACCOUNT_ID) String businessAccountId,
            @Parameter(name = "Channel", in = ParameterIn.HEADER, required = true, example = DTOExamples.CHANNEL) String channel,
            @Parameter(name = "dataListId", in = ParameterIn.PATH, required = true, example = DTOExamples.DATA_LIST_ID) @PathVariable UUID dataListId,
            @Parameter(name = "showDatalistMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = DataListRestDTOMapper.Mode._SHOW_OPTIONS) DataListRestDTOMapper.Mode showDatalistMode) {
        DataListRsDTOv1 rs = new DataListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.dataList = dataListRestDTOMapper.convert(
                    dataListService.findDataList(apiUser, dataListId), new MapperProperties().setMode(showDatalistMode));
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
    public ResponseEntity<?> dataListV1(
            @Parameter(name = "showDatalistMode", in = ParameterIn.QUERY) @RequestParam(defaultValue = DataListRestDTOMapper.Mode._SHOW_OPTIONS) DataListRestDTOMapper.Mode showDatalistMode,
            @RequestBody DataListSearchRqDTOv1 request) {
        DataListSearchRsDTOv1 rs = new DataListSearchRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.dataListList(
                    dataListRestDTOMapper.convertList(
                            dataListService.findDataLists(apiUser, request.dataListIdList()), new MapperProperties().setMode(showDatalistMode)));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
