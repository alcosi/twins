package org.twins.core.controller.rest.priv.system;

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
import org.twins.core.dto.rest.featurer.FeaturerCountRqDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerCountRsDTOv1;
import org.twins.core.mappers.rest.featurer.FeaturerCountRestDTOMapper;
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
public class FeaturerCountController extends ApiController {
    private final FeaturerSearchService featurerSearchService;
    private final FeaturerSearchDTOReverseMapper featurerSearchDTOReverseMapper;
    private final FeaturerCountRestDTOMapper featurerCountRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "featurerCountV1", summary = "Return count of featurers grouped by specified fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Featurer count result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FeaturerCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/featurer/count/v1")
    public ResponseEntity<?> featurerCountV1(
            @MapperContextBinding(roots = FeaturerCountRestDTOMapper.class, response = FeaturerCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid FeaturerCountRqDTOv1 request) {
        FeaturerCountRsDTOv1 rs = new FeaturerCountRsDTOv1();
        try {
            var results =
                    featurerSearchService.countByGroupFields(featurerSearchDTOReverseMapper
                            .convert(request.getSearch(), mapperContext), request.getGroupFields(), pagination);
            rs
                    .setCounts(featurerCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
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
