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
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListOptionMapRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionMapRsDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionRsDTOv3;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Get data list option", name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DATA_LIST_OPTION_MANAGE, Permissions.DATA_LIST_OPTION_VIEW})
public class DataListOptionController extends ApiController {
    private final DataListOptionService dataListOptionService;
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListOptionViewV1", summary = "Returns list data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionRsDTOv3.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/data_list_option/{dataListOptionId}/v1")
    public ResponseEntity<?> dataListOptionV1(
            @MapperContextBinding(roots = DataListOptionRestDTOMapper.class, response = DataListOptionRsDTOv3.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.DATA_LIST_OPTION_ID) @PathVariable UUID dataListOptionId) {
        DataListOptionRsDTOv3 rs = new DataListOptionRsDTOv3();
        try {
            DataListOptionEntity dataListOptionEntity = dataListOptionService.findEntitySafe(dataListOptionId);
            rs
                    .setOption(dataListOptionRestDTOMapper.convert(dataListOptionEntity, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @Deprecated //no pagination support
    @ParametersApiUserHeaders
    @Operation(operationId = "dataListOptionsMapViewV1", summary = "Returns map option id ref list data option")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map {option id/list data option} prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionMapRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/data_list_option/map/v1", method = RequestMethod.POST)
    public ResponseEntity<?> dataListsOptionsMapV1(
            @MapperContextBinding(roots = DataListOptionRestDTOMapper.class, response = DataListOptionMapRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody DataListOptionMapRqDTOv1 request) {
        DataListOptionMapRsDTOv1 rs = new DataListOptionMapRsDTOv1();
        try {
            rs
                    .setDataListOptionMap(dataListOptionRestDTOMapper.convertMap(
                            dataListOptionService.findDataListOptionsByIds(request.dataListOptionIdSet()).getMap(), mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }


}
