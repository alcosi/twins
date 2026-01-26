package org.twins.core.controller.rest.priv.twinflow;

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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dto.rest.twinflow.TwinflowFactorySearchRqDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowFactorySearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TwinflowFactoryRestDTOMapperV1;
import org.twins.core.mappers.rest.twinflow.TwinflowFactorySearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowFactorySearchService;

@Tag(name = ApiTag.TWINFLOW)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWINFLOW_FACTORY_MANAGE, Permissions.TWINFLOW_FACTORY_VIEW})
public class TwinflowFactorySearchController extends ApiController {

    private final TwinflowFactorySearchRestDTOReverseMapper twinflowFactorySearchRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final TwinflowFactorySearchService twinflowFactorySearchService;
    private final TwinflowFactoryRestDTOMapperV1 twinflowFactoryRestDTOMapperV1;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinflowFactorySearchV1", summary = "Returns twinflow factory search result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twinflow factory list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinflowFactorySearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twinflow/factory/search/v1")
    public ResponseEntity<?> twinflowFactorySearchV1(
            @MapperContextBinding(roots = TwinflowFactoryRestDTOMapperV1.class, response = TwinflowFactorySearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinflowFactorySearchRqDTOv1 request) {
        TwinflowFactorySearchRsDTOv1 rs = new TwinflowFactorySearchRsDTOv1();

        try {
            PaginationResult<TwinflowFactoryEntity> twinflowFactoryList = twinflowFactorySearchService.findTwinflowFactory(twinflowFactorySearchRestDTOReverseMapper.convert(request.getSearch()), pagination);

            rs
                    .setTwinflowFactories(twinflowFactoryRestDTOMapperV1.convertCollection(twinflowFactoryList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twinflowFactoryList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
