package org.twins.core.controller.rest.priv.datalist;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.*;
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionProjectionSearchRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionProjectionSearchRsDTOv1;
import org.twins.core.mappers.rest.datalist.DataListOptionProjectionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionProjectionSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.datalist.DataListOptionProjectionSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DATA_LIST_OPTION_MANAGE, Permissions.DATA_LIST_OPTION_VIEW})
public class DataListOptionProjectionSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final DataListOptionProjectionSearchService dataListOptionProjectionSearchService;
    private final DataListOptionProjectionSearchDTOReverseMapper dataListOptionProjectionSearchDTOReverseMapper;
    private final DataListOptionProjectionRestDTOMapper dataListOptionProjectionRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListOptionProjectionSearchV1", summary = "Returns lists data list option projections")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data list option projections prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionProjectionSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/data_list_option_projection/search/v1")
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> dataListOptionProjectionSearchV1(
            @MapperContextBinding(roots = DataListOptionProjectionRestDTOMapper.class, response = DataListOptionProjectionSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody DataListOptionProjectionSearchRqDTOv1 request) {
        DataListOptionProjectionSearchRsDTOv1 rs = new DataListOptionProjectionSearchRsDTOv1();
        try {
            PaginationResult<DataListOptionProjectionEntity> dataListOptionProjectionsList = dataListOptionProjectionSearchService.findDataListOptionProjections(dataListOptionProjectionSearchDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setPagination(paginationMapper.convert(dataListOptionProjectionsList))
                    .setDataListOptionProjections(dataListOptionProjectionRestDTOMapper.convertCollection(dataListOptionProjectionsList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
