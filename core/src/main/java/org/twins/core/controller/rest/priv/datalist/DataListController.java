package org.twins.core.controller.rest.priv.datalist;

import io.swagger.v3.oas.annotations.Operation;
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
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.dto.rest.datalist.DataListRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListRsDTOv1;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;

import java.util.List;

@Tag(description = "Get data lists", name = "dataList")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DataListController extends ApiController {
    private final AuthService authService;
    private final DataListService dataListService;
    private final DataListRestDTOMapper dataListRestDTOMapper;
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @Operation(operationId = "dataListV1", summary = "Returns list details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/data_list/v1", method = RequestMethod.POST)
    public ResponseEntity<?> dataListV1(
            @RequestHeader("UserId") String userId,
            @RequestHeader("DomainId") String domainId,
            @RequestHeader("BusinessAccountId") String businessAccountId,
            @RequestHeader("Channel") String channel,
            @RequestBody DataListRqDTOv1 request) {
        DataListRsDTOv1 rs = new DataListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            List<DataListEntity> dataLists = dataListService.findDataLists(apiUser, request.dataListIdList());
            rs.dataListList(dataListRestDTOMapper.convertList(dataLists));
            if (request.showOptions()) {
                for (DataListDTOv1 dataListDTOv1 : rs.dataListList()) {
                    dataListDTOv1.options(
                            dataListOptionRestDTOMapper.convertList(
                                    dataListService.findDataListOptions(apiUser, dataListDTOv1.id())));
                }
            }
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
