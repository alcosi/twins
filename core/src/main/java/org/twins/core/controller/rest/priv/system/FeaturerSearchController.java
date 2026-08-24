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
import org.cambium.featurer.dao.FeaturerEntity;
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
import org.twins.core.dto.rest.featurer.FeaturerSearchRqDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerSearchRsDTOv1;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.system.FeaturerSearchService;

@Tag(description = "", name = ApiTag.SYSTEM)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FEATURER_MANAGE, Permissions.FEATURER_VIEW})
public class FeaturerSearchController extends ApiController {
    private final FeaturerRestDTOMapper featurerRestDTOMapper;
    private final FeaturerSearchDTOReverseMapper featurerSearchDTOReverseMapper;
    private final FeaturerSearchService featurerSearchService;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "featurerSearchV1", summary = "Featurer search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Featurer data result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FeaturerSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/featurer/search/v1")
    public ResponseEntity<?> featurerSearchV1(
            @MapperContextBinding(roots = FeaturerRestDTOMapper.class, response = FeaturerSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FeaturerSearchRqDTOv1 request) {
        FeaturerSearchRsDTOv1 rs = new FeaturerSearchRsDTOv1();
        try {
            PaginationResult<FeaturerEntity> featurers = featurerSearchService
                    .search(featurerSearchDTOReverseMapper.convert(request.getSearch(), mapperContext), pagination, request.getSortField(), request.getSortDirection());
            rs
                    .setFeaturerList(featurerRestDTOMapper.convertCollection(featurers.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(featurers))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
