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
import org.twins.core.domain.datalist.DataListOptionUpdate;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.datalist.DataListOptionRsDTOv3;
import org.twins.core.dto.rest.datalist.DataListOptionUpdateRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionUpdateRqDTOv2;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionUpdateDTOReverseMapperV2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.permission.Permissions;

import java.util.List;
import java.util.UUID;


@Tag(name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DATA_LIST_OPTION_MANAGE, Permissions.DATA_LIST_OPTION_UPDATE})
public class DataListOptionUpdateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final DataListOptionUpdateDTOReverseMapper dataListOptionUpdateDTOReverseMapper;
    private final DataListOptionUpdateDTOReverseMapperV2 dataListOptionUpdateDTOReverseMapperV2;
    private final DataListOptionService dataListOptionService;
    private final DataListOptionRestDTOMapper DataListOptionRestDTOMapper;

    @Deprecated
    @ParametersApiUserHeaders
    @Operation(operationId = "dataListOptionUpdateV1", summary = "Data list option for update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated data list option data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionRsDTOv3.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/data_list_option/{dataListOptionId}/v1")
    public ResponseEntity<?> dataListOptionUpdateV1(
            @MapperContextBinding(roots = DataListOptionRestDTOMapper.class, response = DataListOptionRsDTOv3.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.DATA_LIST_OPTION_ID) @PathVariable UUID dataListOptionId,
            @RequestBody DataListOptionUpdateRqDTOv1 request) {
        DataListOptionRsDTOv3 rs = new DataListOptionRsDTOv3();
        try {
            DataListOptionEntity dataListOption = dataListOptionService.updateDataListOptions(dataListOptionUpdateDTOReverseMapper.convert(request).setId(dataListOptionId));
            rs
                    .setOption(DataListOptionRestDTOMapper.convert(dataListOption, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListOptionUpdateV2", summary = "Update data list option batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data list option batch updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/data_list_option/v2")
    public ResponseEntity<?> dataListOptionUpdateV2(
            @RequestBody DataListOptionUpdateRqDTOv2 request) {
        DataListOptionRsDTOv3 rs = new DataListOptionRsDTOv3();
        try {
            List<DataListOptionUpdate> dataListOptionUpdates = dataListOptionUpdateDTOReverseMapperV2.convertCollection(request.getDataListOptions());

            dataListOptionService.updateDataListOptions(dataListOptionUpdates);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
