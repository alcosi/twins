package org.twins.core.controller.rest.priv.datalist;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.datalist.DataListSubsetDeleteRqDTOv1;
import org.twins.core.service.datalist.DataListSubsetService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.DATA_LIST_SUBSET_DELETE)
public class DataListSubsetDeleteController extends ApiController {
    private final DataListSubsetService dataListSubsetService;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListSubsetDeleteV1", summary = "Data list subset delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data list subsets deleted"),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/data_list_subset/delete/v1")
    public ResponseEntity<?> dataListSubsetDeleteV1(
            @RequestBody DataListSubsetDeleteRqDTOv1 request) {
        Response rs = new Response();
        try {
            //todo add usage check
            dataListSubsetService.deleteDataListSubsets(request.getDataListSubsetIdList());
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
