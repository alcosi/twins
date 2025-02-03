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
import org.cambium.featurer.FeaturerService;
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
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dto.rest.featurer.FeaturerSearchRqDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerSearchRsDTOv1;
import org.twins.core.mappers.rest.featurer.FeaturerDTOReversMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;

@Tag(description = "", name = ApiTag.SYSTEM)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FeaturerSearchController extends ApiController {
    private final FeaturerRestDTOMapper featurerRestDTOMapper;
    private final FeaturerDTOReversMapper featurerDTOReversMapper;
    private final FeaturerService featurerService;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "featurerSearchV1", summary = "Featurer search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Featurer data result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FeaturerSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/featurer/search/v1")
    public ResponseEntity<?> featurerSearchV1(
            @MapperContextBinding(roots = FeaturerRestDTOMapper.class, response = FeaturerSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FeaturerSearchRqDTOv1 request) {
        FeaturerSearchRsDTOv1 rs = new FeaturerSearchRsDTOv1();
        try {
            PaginationResult<FeaturerEntity> featurers = featurerService
                    .findFeaturers(featurerDTOReversMapper.convert(request), pagination);
            rs
                    .setFeaturerList(featurerRestDTOMapper.convertCollection(featurers.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(featurers));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
