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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListRsDTOv1;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapperV2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
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
    private final DataListRestDTOMapperV2 dataListRestDTOMapperV2;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListViewV1", summary = "Returns list data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/data_list/{dataListId}/v1")
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> dataListViewV1(
            @MapperContextBinding(roots = DataListRestDTOMapperV2.class, response = DataListRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.DATA_LIST_ID) @PathVariable UUID dataListId) {
        DataListRsDTOv1 rs = new DataListRsDTOv1();
        try {
            rs.dataList = dataListRestDTOMapperV2.convert(dataListService.findEntitySafe(dataListId), mapperContext);
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
    @GetMapping(value = "/private/data_list_by_key/{dataListKey}/v1")
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> dataListByKeyViewV1(
            @MapperContextBinding(roots = DataListRestDTOMapperV2.class, response = DataListRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.DATA_LIST_KEY) @PathVariable String dataListKey) {
        DataListRsDTOv1 rs = new DataListRsDTOv1();
        try {
            rs.dataList = dataListRestDTOMapperV2.convert(
                    dataListService.findEntitySafe(dataListKey), mapperContext);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
