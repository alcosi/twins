package org.twins.core.controller.rest.priv.factory;

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
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterSearchRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryMultiplierFilerSearchDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryMultiplierFilterRestDTOMapperV2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryMultiplierFilterSearchService;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FactoryMultiplierFilterSearchController extends ApiController {

    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryMultiplierFilterSearchService factoryMultiplierFilterSearchService;
    private final FactoryMultiplierFilterRestDTOMapperV2 factoryMultiplierFilterRestDTOMapperV2;
    private final FactoryMultiplierFilerSearchDTOReverseMapper factoryMultiplierFilerSearchDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryMultiplierFilterSearchV1", summary = "Factory multiplier filter search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory multiplier filter list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryMultiplierFilterSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_multiplier_filter/search/v1")
    public ResponseEntity<?> factoryMultiplierFilterSearchV1(
            @MapperContextBinding(roots = FactoryMultiplierFilterRestDTOMapperV2.class, response = FactoryMultiplierFilterSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryMultiplierFilterSearchRqDTOv1 request) {
        FactoryMultiplierFilterSearchRsDTOv1 rs = new FactoryMultiplierFilterSearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryMultiplierFilterEntity> multiplierFilter = factoryMultiplierFilterSearchService
                    .findFactoryMultiplierFilters(factoryMultiplierFilerSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setMultiplierFilters(factoryMultiplierFilterRestDTOMapperV2.convertCollection(multiplierFilter.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(multiplierFilter))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
