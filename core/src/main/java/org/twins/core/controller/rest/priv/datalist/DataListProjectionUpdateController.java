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
import org.twins.core.dao.datalist.DataListProjectionEntity;
import org.twins.core.dto.rest.datalist.DataListProjectionListRsDTOv1;
import org.twins.core.dto.rest.datalist.DataListProjectionUpdateRqDTOv1;
import org.twins.core.mappers.rest.datalist.DataListProjectionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListProjectionUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.datalist.DataListProjectionService;
import org.twins.core.service.permission.Permissions;

import java.util.List;


@Tag(description = "", name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DATA_LIST_MANAGE, Permissions.DATA_LIST_UPDATE})
public class DataListProjectionUpdateController extends ApiController {
    private final DataListProjectionUpdateDTOReverseMapper dataListProjectionUpdateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final DataListProjectionService dataListProjectionService;
    private final DataListProjectionRestDTOMapper dataListProjectionRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListProjectionUpdateV1", summary = "Data list projection update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data list projection updated", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListProjectionListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/data_list_projection/v1")
    public ResponseEntity<?> dataListProjectionUpdateV1(
            @MapperContextBinding(roots = DataListProjectionRestDTOMapper.class, response = DataListProjectionListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody DataListProjectionUpdateRqDTOv1 request) {
        DataListProjectionListRsDTOv1 rs = new DataListProjectionListRsDTOv1();
        try {
            List<DataListProjectionEntity> dataListProjectionEntities = dataListProjectionService.updateDataListProjections(dataListProjectionUpdateDTOReverseMapper.convertCollection(request.getDataListProjectionList()));
            rs
                    .setDataListProjections(dataListProjectionRestDTOMapper.convertCollection(dataListProjectionEntities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
