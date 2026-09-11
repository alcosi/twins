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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.dto.rest.datalist.DataListSubsetListRsDTOv1;
import org.twins.core.dto.rest.datalist.DataListSubsetUpdateRqDTOv1;
import org.twins.core.mappers.rest.datalist.DataListSubsetRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListSubsetUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.datalist.DataListSubsetService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.DATA_LIST_SUBSET_UPDATE)
public class DataListSubsetUpdateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final DataListSubsetUpdateDTOReverseMapper dataListSubsetUpdateDTOReverseMapper;
    private final DataListSubsetRestDTOMapper dataListSubsetRestDTOMapper;
    private final DataListSubsetService dataListSubsetService;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListSubsetUpdateV1", summary = "Data list subset update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data list subsets updated", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListSubsetListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/data_list_subset/v1")
    public ResponseEntity<?> dataListSubsetUpdateV1(
            @MapperContextBinding(roots = DataListSubsetRestDTOMapper.class, response = DataListSubsetListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody DataListSubsetUpdateRqDTOv1 request) {
        DataListSubsetListRsDTOv1 rs = new DataListSubsetListRsDTOv1();
        try {
            List<DataListSubsetEntity> entities = dataListSubsetService.updateDataListSubsets(dataListSubsetUpdateDTOReverseMapper.convertCollection(request.getDataListSubsets()));
            rs
                    .setDataListSubsets(dataListSubsetRestDTOMapper.convertCollection(entities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
