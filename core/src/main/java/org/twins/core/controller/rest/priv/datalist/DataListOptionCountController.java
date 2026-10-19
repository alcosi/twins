package org.twins.core.controller.rest.priv.datalist;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
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
import org.twins.core.dto.rest.datalist.DataListOptionCountRqDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionCountRsDTOv1;
import org.twins.core.mappers.rest.datalist.DataListOptionCountRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.datalist.DataListOptionSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.DATA_LIST)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DATA_LIST_OPTION_MANAGE, Permissions.DATA_LIST_OPTION_VIEW})
public class DataListOptionCountController extends ApiController {
    private final DataListOptionSearchService dataListOptionSearchService;
    private final DataListOptionSearchDTOReverseMapper dataListOptionSearchDTOReverseMapper;
    private final DataListOptionCountRestDTOMapper dataListOptionCountRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "dataListOptionCountV1", summary = "Return count of data list options grouped by specified fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DataListOptionCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/data_list_option/count/v1")
    public ResponseEntity<?> dataListOptionCountV1(
            @MapperContextBinding(roots = DataListOptionCountRestDTOMapper.class, response = DataListOptionCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid DataListOptionCountRqDTOv1 request) {
        DataListOptionCountRsDTOv1 rs = new DataListOptionCountRsDTOv1();
        try {
            var results =
                    dataListOptionSearchService.countByGroupFields(dataListOptionSearchDTOReverseMapper
                            .convert(request.getSearch(), mapperContext), request.getGroupFields(), pagination);
            rs
                    .setCounts(dataListOptionCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(results))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
