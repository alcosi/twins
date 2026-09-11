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
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.dto.rest.datalist.DataListSubsetSearchRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListSubsetSearchRsDTOv1;
import org.twins.core.mappers.rest.datalist.DataListSubsetRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListSubsetSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.datalist.DataListSubsetSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DATA_LIST_SUBSET_MANAGE, Permissions.DATA_LIST_SUBSET_VIEW})
public class DataListSubsetSearchController extends ApiController {
    private final DataListSubsetRestDTOMapper dataListSubsetRestDTOMapper;
    private final DataListSubsetSearchDTOReverseMapper dataListSubsetSearchDTOReverseMapper;
    private final DataListSubsetSearchService dataListSubsetSearchService;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListSubsetSearchV1", summary = "Data list subset search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data list subset data result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListSubsetSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/data_list_subset/search/v1")
    public ResponseEntity<?> dataListSubsetSearchV1(
            @MapperContextBinding(roots = DataListSubsetRestDTOMapper.class, response = DataListSubsetSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody DataListSubsetSearchRqDTOv1 request) {
        DataListSubsetSearchRsDTOv1 rs = new DataListSubsetSearchRsDTOv1();
        try {
            PaginationResult<DataListSubsetEntity> dataListSubsets = dataListSubsetSearchService
                    .search(dataListSubsetSearchDTOReverseMapper.convert(request.getSearch(), mapperContext), pagination, request.getSortField(), request.getSortDirection());
            rs
                    .setPagination(paginationMapper.convert(dataListSubsets))
                    .setDataListSubsets(dataListSubsetRestDTOMapper.convertCollection(dataListSubsets.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
