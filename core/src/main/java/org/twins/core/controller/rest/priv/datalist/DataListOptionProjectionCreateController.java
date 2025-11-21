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
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionProjectionCreateRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionProjectionListRsDTOv1;
import org.twins.core.mappers.rest.datalist.DataListOptionProjectionCreateDTOReverseMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionProjectionRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.datalist.DataListOptionProjectionService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DATA_LIST_OPTION_MANAGE, Permissions.DATA_LIST_OPTION_CREATE})
public class DataListOptionProjectionCreateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final DataListOptionProjectionCreateDTOReverseMapper dataListOptionProjectionCreateDTOReverseMapper;
    private final DataListOptionProjectionRestDTOMapper dataListOptionProjectionRestDTOMapper;
    private final DataListOptionProjectionService dataListOptionProjectionService;


    @ParametersApiUserHeaders
    @Operation(operationId = "dataListOptionProjectionCreateV1", summary = "Data list option projection create")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data list option projection created", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionProjectionListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/data_list_option_projection/v1")
    public ResponseEntity<?> dataListOptionProjectionCreateV1(
            @MapperContextBinding(roots = DataListOptionProjectionRestDTOMapper.class, response = DataListOptionProjectionListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody DataListOptionProjectionCreateRqDTOv1 request) {
        DataListOptionProjectionListRsDTOv1 rs = new DataListOptionProjectionListRsDTOv1();
        try {
            List<DataListOptionProjectionEntity> dataListOptionProjectionEntities = dataListOptionProjectionService.createDataListOptionProjections(dataListOptionProjectionCreateDTOReverseMapper.convertCollection(request.getDataListOptionProjectionList()));
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
