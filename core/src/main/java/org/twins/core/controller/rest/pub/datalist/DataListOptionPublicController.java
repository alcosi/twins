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
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserAnonymousHeaders;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListOptionMapRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionMapRsDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionRsDTOv1;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.datalist.DataListService;

import java.util.UUID;

@Tag(description = "Get public data list option", name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DataListOptionPublicController extends ApiController {
    private final AuthService authService;
    private final DataListService dataListService;
    private final DataListOptionService dataListOptionService;
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @ParametersApiUserAnonymousHeaders
    @Operation(operationId = "dataListOptionPublicViewV1", summary = "Returns public list data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Public list details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/public/data_list_option/{dataListOptionId}/v1")
    public ResponseEntity<?> dataListOptionPublicViewV1(
            @MapperContextBinding(roots = DataListOptionRestDTOMapper.class, response = DataListOptionRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.DATA_LIST_OPTION_ID) @PathVariable UUID dataListOptionId) {
        DataListOptionRsDTOv1 rs = new DataListOptionRsDTOv1();
        try {
            authService.getApiUser().setAnonymous();
            DataListOptionEntity dataListOptionEntity = dataListOptionService.findEntitySafe(dataListOptionId);
            rs
                    .setDataListId(dataListOptionEntity.getDataListId())
                    .setOption(dataListOptionRestDTOMapper.convert(dataListOptionEntity, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @Deprecated //no pagination support
    @ParametersApiUserAnonymousHeaders
    @Operation(operationId = "dataListOptionsMapViewPublicV1", summary = "Returns map option id ref list data option")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map {option id/list data option} prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionMapRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/public/data_list_option/map/v1")
    public ResponseEntity<?> dataListOptionsMapViewPublicV1(
            @MapperContextBinding(roots = DataListOptionRestDTOMapper.class, response = DataListOptionMapRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody DataListOptionMapRqDTOv1 request) {
        DataListOptionMapRsDTOv1 rs = new DataListOptionMapRsDTOv1();
        try {
            authService.getApiUser().setAnonymous();
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
