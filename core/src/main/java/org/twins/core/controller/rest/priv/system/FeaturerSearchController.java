package org.twins.core.controller.rest.priv.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.featurer.FeaturerSearchRqDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerSearchRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.featurer.FeaturerDTOReversMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.service.featurer.FeaturerSearchResult;

import static org.cambium.common.util.PaginationUtils.DEFAULT_VALUE_LIMIT;
import static org.cambium.common.util.PaginationUtils.DEFAULT_VALUE_OFFSET;

@Tag(description = "", name = ApiTag.SYSTEM)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FeaturerSearchController extends ApiController {
    final FeaturerRestDTOMapper featurerRestDTOMapper;
    final FeaturerDTOReversMapper featurerDTOReversMapper;
    final FeaturerService featurerService;
    final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "featurerListV1", summary = "Featurer search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Featurer data result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FeaturerSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/featurer/v1")
    public ResponseEntity<?> featurerListV1(
        MapperContext mapperContext,
        @RequestParam(name = RestRequestParam.showFeaturerMode, defaultValue = FeaturerRestDTOMapper.Mode._SHORT) FeaturerRestDTOMapper.Mode showFeaturerMode,
        @RequestParam(name = RestRequestParam.showFeaturerParamMode, defaultValue = FeaturerRestDTOMapper.ShowFeaturerParamMode._SHOW) FeaturerRestDTOMapper.ShowFeaturerParamMode showFeaturerParamMode,
        @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
        @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit,
        @RequestBody FeaturerSearchRqDTOv1 request) {
        FeaturerSearchRsDTOv1 rs = new FeaturerSearchRsDTOv1();
        try {
            FeaturerSearchResult featurers = featurerService
                    .findFeaturers(featurerDTOReversMapper.convert(request), offset, limit);
            rs
                    .setPagination(paginationMapper.convert(featurers))
                    .setFeaturerList(featurerRestDTOMapper.convertCollection(featurers.getFeaturerList(), mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
