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
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListOptionSearchConfiguredRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionSearchRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionSearchRsDTOv1;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.datalist.DataListOptionSearchService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DATA_LIST_OPTION_MANAGE, Permissions.DATA_LIST_OPTION_VIEW})
public class DataListOptionSearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;

    private final DataListOptionSearchDTOReverseMapper dataListOptionSearchDTOReverseMapper;
    private final DataListOptionSearchService dataListOptionSearchService;
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListOptionSearchListV1", summary = "Return a list of all data list option for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/data_list_option/search/v1")
    public ResponseEntity<?> dataListOptionSearchListV1(
            @MapperContextBinding(roots = DataListOptionRestDTOMapper.class, response = DataListOptionSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody DataListOptionSearchRqDTOv1 request,
            @SimplePaginationParams(sortField = {DataListOptionEntity.Fields.option, DataListOptionEntity.Fields.externalId}) SimplePagination pagination) {
        DataListOptionSearchRsDTOv1 rs = new DataListOptionSearchRsDTOv1();
        try {
            PaginationResult<DataListOptionEntity> dataListOptionList = dataListOptionSearchService
                    .findDataListOptionForDomain(dataListOptionSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setOptions(dataListOptionRestDTOMapper.convertCollection(dataListOptionList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(dataListOptionList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListOptionSearchConfiguredV1", summary = "Data list options by search id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/data_list_option/search/{searchId}/v1")
    public ResponseEntity<?> dataListOptionSearchConfiguredV1(
            @MapperContextBinding(roots = DataListOptionRestDTOMapper.class, response = DataListOptionSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @Parameter(example = DTOExamples.SEARCH_ID) @PathVariable UUID searchId,
            @RequestBody DataListOptionSearchConfiguredRqDTOv1 request) {
        DataListOptionSearchRsDTOv1 rs = new DataListOptionSearchRsDTOv1();
        try {
            PaginationResult<DataListOptionEntity> dataListOptionList = dataListOptionSearchService
                    .findDataListOptions(searchId, request.getParams(), dataListOptionSearchDTOReverseMapper.convert(request.getNarrow()), pagination);
            rs
                    .setOptions(dataListOptionRestDTOMapper.convertCollection(dataListOptionList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(dataListOptionList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
