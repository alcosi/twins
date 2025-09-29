package org.twins.core.controller.rest.pub.datalist;

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
import org.twins.core.controller.rest.annotation.ParametersApiUserAnonymousHeaders;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionSearchRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionSearchRsDTOv1;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListOptionSearchService;

@Tag(name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DataListOptionSearchPublicController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final AuthService authService;
    private final DataListOptionSearchDTOReverseMapper dataListOptionSearchDTOReverseMapper;
    private final DataListOptionSearchService dataListOptionSearchService;
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @ParametersApiUserAnonymousHeaders
    @Operation(operationId = "dataListOptionSearchPublicListV1", summary = "Return a list of all public data list options")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/public/data_list_option/search/v1")
    public ResponseEntity<?> dataListOptionSearchPublicListV1(
            @MapperContextBinding(roots = DataListOptionRestDTOMapper.class, response = DataListOptionSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody DataListOptionSearchRqDTOv1 request,
            @SimplePaginationParams(sortField = DataListOptionEntity.Fields.option) SimplePagination pagination) {
        DataListOptionSearchRsDTOv1 rs = new DataListOptionSearchRsDTOv1();
        try {
            authService.getApiUser().setAnonymous();
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
}
