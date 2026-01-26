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
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionProjectionListRsDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionProjectionUpdateRqDTOv1;
import org.twins.core.mappers.rest.datalist.DataListOptionProjectionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionProjectionUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.datalist.DataListOptionProjectionService;
import org.twins.core.service.permission.Permissions;

import java.util.List;


@Tag(description = "", name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DATA_LIST_OPTION_MANAGE, Permissions.DATA_LIST_OPTION_UPDATE})
public class DataListOptionProjectionUpdateController extends ApiController {
    private final DataListOptionProjectionUpdateDTOReverseMapper dataListOptionProjectionUpdateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final DataListOptionProjectionService dataListOptionProjectionService;
    private final DataListOptionProjectionRestDTOMapper dataListOptionProjectionRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListOptionProjectionUpdateV1", summary = "Data list option projection update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data list option projection updated", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionProjectionListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/data_list_option_projection/v1")
    public ResponseEntity<?> dataListOptionProjectionUpdateV1(
            @MapperContextBinding(roots = DataListOptionProjectionRestDTOMapper.class, response = DataListOptionProjectionListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody DataListOptionProjectionUpdateRqDTOv1 request) {
        DataListOptionProjectionListRsDTOv1 rs = new DataListOptionProjectionListRsDTOv1();
        try {
            List<DataListOptionProjectionEntity> dataListOptionProjectionEntities = dataListOptionProjectionService.updateDataListOptionProjections(dataListOptionProjectionUpdateDTOReverseMapper.convertCollection(request.getDataListOptionProjections()));
            rs
                    .setDataListOptionProjections(dataListOptionProjectionRestDTOMapper.convertCollection(dataListOptionProjectionEntities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
