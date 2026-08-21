package org.twins.core.controller.rest.priv.system;

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
import org.cambium.featurer.dao.FeaturerTypeEntity;
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
import org.twins.core.dto.rest.featurer.FeaturerTypeSearchRqDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerTypeSearchRsDTOv1;
import org.twins.core.mappers.rest.featurer.FeaturerTypeRestDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerTypeSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.system.FeaturerTypeSearchService;

@Tag(description = "", name = ApiTag.SYSTEM)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FEATURER_MANAGE, Permissions.FEATURER_VIEW})
public class FeaturerTypeSearchController extends ApiController {
    private final FeaturerTypeRestDTOMapper featurerTypeRestDTOMapper;
    private final FeaturerTypeSearchDTOReverseMapper featurerTypeSearchDTOReverseMapper;
    private final FeaturerTypeSearchService featurerTypeSearchService;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "featurerTypeSearchV1", summary = "Featurer type search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Featurer type data result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FeaturerTypeSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/featurer_type/search/v1")
    public ResponseEntity<?> featurerTypeSearchV1(
            @MapperContextBinding(roots = FeaturerTypeRestDTOMapper.class, response = FeaturerTypeSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FeaturerTypeSearchRqDTOv1 request) {
        FeaturerTypeSearchRsDTOv1 rs = new FeaturerTypeSearchRsDTOv1();
        try {
            PaginationResult<FeaturerTypeEntity> featurerTypes = featurerTypeSearchService
                    .search(featurerTypeSearchDTOReverseMapper.convert(request.getSearch(), mapperContext), pagination, request.getSortField(), request.getSortDirection());
            rs
                    .setFeaturerTypeList(featurerTypeRestDTOMapper.convertCollection(featurerTypes.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(featurerTypes));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
