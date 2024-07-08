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
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.featurer.FeaturerSearchRqDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.featurer.FeaturerDTOReversMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.cambium.common.pagination.PaginationResult;

import static org.cambium.common.util.PaginationUtils.*;

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
    @Operation(operationId = "featurerListV1", summary = "Featurer search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Featurer data result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FeaturerSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/featurer/v1")
    public ResponseEntity<?> featurerListV1(
        @MapperContextBinding(roots = FeaturerRestDTOMapper.class, response = FeaturerSearchRsDTOv1.class) MapperContext mapperContext,
        @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
        @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit,
        @RequestBody FeaturerSearchRqDTOv1 request) {
        FeaturerSearchRsDTOv1 rs = new FeaturerSearchRsDTOv1();
        try {
            PaginationResult<FeaturerEntity> featurers = featurerService
                    .findFeaturers(featurerDTOReversMapper.convert(request), createSimplePagination(offset, limit, Sort.unsorted()));
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
