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
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv2;
import org.twins.core.dto.rest.datalist.DataListOptionRsDTOv1;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapperV2;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;

import java.util.UUID;

@Tag(description = "Get data list option", name = "dataList")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DataListOptionController extends ApiController {
    private final AuthService authService;
    private final DataListService dataListService;
    private final DataListOptionRestDTOMapperV2 dataListOptionRestDTOMapperV2;

    @Operation(operationId = "dataListOptionViewV1", summary = "Returns list deta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/data_list_option/{dataListOptionId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> dataListV1(
            @Parameter(name = "UserId", in = ParameterIn.HEADER, required = true, example = DTOExamples.USER_ID) String userId,
            @Parameter(name = "DomainId", in = ParameterIn.HEADER, required = true, example = DTOExamples.DOMAIN_ID) String domainId,
            @Parameter(name = "BusinessAccountId", in = ParameterIn.HEADER, required = true, example = DTOExamples.BUSINESS_ACCOUNT_ID) String businessAccountId,
            @Parameter(name = "Channel", in = ParameterIn.HEADER, required = true, example = DTOExamples.CHANNEL) String channel,
            @Parameter(name = "dataListOptionId", in = ParameterIn.PATH, required = true, example = DTOExamples.DATA_LIST_OPTION_ID) @PathVariable UUID dataListOptionId) {
        DataListOptionRsDTOv1 rs = new DataListOptionRsDTOv1();
        try {
            rs.option(dataListOptionRestDTOMapperV2.convert(
                    dataListService.findDataListOption(dataListOptionId)));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }


}
