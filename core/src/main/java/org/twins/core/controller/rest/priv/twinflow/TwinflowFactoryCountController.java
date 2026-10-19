package org.twins.core.controller.rest.priv.twinflow;

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
import org.twins.core.dto.rest.twinflow.TwinflowFactoryCountRqDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryCountRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TwinflowFactoryCountRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowFactorySearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowFactorySearchService;

@Tag(name = ApiTag.TWINFLOW)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWINFLOW_FACTORY_MANAGE, Permissions.TWINFLOW_FACTORY_VIEW})
public class TwinflowFactoryCountController extends ApiController {
    private final TwinflowFactorySearchService twinflowFactorySearchService;
    private final TwinflowFactorySearchRestDTOReverseMapper twinflowFactorySearchRestDTOReverseMapper;
    private final TwinflowFactoryCountRestDTOMapper twinflowFactoryCountRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinflowFactoryCountV1", summary = "Return count of twinflow factories grouped by specified fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinflowFactoryCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twinflow/factory/count/v1")
    public ResponseEntity<?> twinflowFactoryCountV1(
            @MapperContextBinding(roots = TwinflowFactoryCountRestDTOMapper.class, response = TwinflowFactoryCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid TwinflowFactoryCountRqDTOv1 request) {
        TwinflowFactoryCountRsDTOv1 rs = new TwinflowFactoryCountRsDTOv1();
        try {
            var results = twinflowFactorySearchService
                    .countByGroupFields(twinflowFactorySearchRestDTOReverseMapper.convert(request.getSearch(), mapperContext), request.getGroupFields(), pagination);
            rs
                    .setCounts(twinflowFactoryCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
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
