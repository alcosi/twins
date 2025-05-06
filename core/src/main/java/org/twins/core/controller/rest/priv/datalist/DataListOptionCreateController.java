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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.domain.datalist.DataListOptionCreate;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.datalist.DataListOptionCreateRqDTOv2;
import org.twins.core.dto.rest.datalist.DataListOptionCreateRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionRsDTOv3;
import org.twins.core.mappers.rest.datalist.DataListOptionCreateDTOReverseMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionCreateDTOReverseMapperV2;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapperV3;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.datalist.DataListOptionService;

import java.util.List;


@Tag(name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DataListOptionCreateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final DataListOptionCreateDTOReverseMapper dataListOptionCreateDTOReverseMapper;
    private final DataListOptionCreateDTOReverseMapperV2 dataListOptionCreateDTOReverseMapperV2;
    private final DataListOptionRestDTOMapperV3 dataListOptionRestDTOMapperV3;
    private final DataListOptionService dataListOptionService;

    @Deprecated
    @ParametersApiUserHeaders
    @Operation(operationId = "dataListOptionCreateV1", summary = "Create data list option data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The data list option was created successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionRsDTOv3.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/data_list_option/v1")
    public ResponseEntity<?> dataListOptionCreateV1(
            @MapperContextBinding(roots = DataListOptionRestDTOMapperV3.class, response = DataListOptionRsDTOv3.class) MapperContext mapperContext,
            @RequestBody DataListOptionCreateRqDTOv1 request) {
        DataListOptionRsDTOv3 rs = new DataListOptionRsDTOv3();
        try {
            List<DataListOptionEntity> dataListOptionEntities = dataListOptionService.createDataListOptions(List.of(dataListOptionCreateDTOReverseMapper.convert(request)));
            rs
                    .setOption(dataListOptionRestDTOMapperV3.convert(dataListOptionEntities.getFirst(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListOptionCreateV2", summary = "Create batch data list options")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The data list option batch was created successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/data_list_option/v2")
    public ResponseEntity<?> dataListOptionCreateV2(
            @RequestBody DataListOptionCreateRqDTOv2 request) {
        Response rs = new Response();
        try {
            List<DataListOptionCreate> dataListOptions = dataListOptionCreateDTOReverseMapperV2.convertCollection(request.getDataListOptions());

            dataListOptionService.createDataListOptions(dataListOptions);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
