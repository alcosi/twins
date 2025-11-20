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
import org.twins.core.dao.datalist.DataListProjectionEntity;
import org.twins.core.dto.rest.datalist.DataListProjectionSearchRsDTOv1;
import org.twins.core.dto.rest.datalist.DataListProjectionSearchRqDTOv1;
import org.twins.core.mappers.rest.datalist.DataListProjectionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListProjectionSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.service.datalist.DataListProjectionSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DATA_LIST_MANAGE, Permissions.DATA_LIST_VIEW})
public class DataListProjectionSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final DataListProjectionSearchService dataListProjectionSearchService;
    private final DataListProjectionSearchDTOReverseMapper dataListProjectionSearchDTOReverseMapper;
    private final DataListProjectionRestDTOMapper dataListProjectionRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListProjectionSearchV1", summary = "Returns lists data list projections")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data list projections prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListProjectionSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/data_list_projection/search/v1")
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> dataListProjectionSearchV1(
            @MapperContextBinding(roots = DataListProjectionRestDTOMapper.class, response = DataListProjectionSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody DataListProjectionSearchRqDTOv1 request) {
        DataListProjectionSearchRsDTOv1 rs = new DataListProjectionSearchRsDTOv1();
        try {
            PaginationResult<DataListProjectionEntity> dataListProjectionsList = dataListProjectionSearchService.findDataListProjections(dataListProjectionSearchDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setDataListProjections(dataListProjectionRestDTOMapper.convertCollection(dataListProjectionsList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(dataListProjectionsList));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
