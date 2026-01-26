package org.twins.core.controller.rest.priv.factory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterSearchRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterViewRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryMultiplierFilerSearchDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryMultiplierFilterRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryMultiplierFilterSearchService;
import org.twins.core.service.factory.FactoryMultiplierFilterService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.MULTIPLIER_MANAGE, Permissions.MULTIPLIER_VIEW})
public class FactoryMultiplierFilterSearchController extends ApiController {

    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryMultiplierFilterSearchService factoryMultiplierFilterSearchService;
    private final FactoryMultiplierFilterRestDTOMapper factoryMultiplierFilterRestDTOMapper;
    private final FactoryMultiplierFilerSearchDTOReverseMapper factoryMultiplierFilerSearchDTOReverseMapper;
    private final FactoryMultiplierFilterService factoryMultiplierFilterService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryMultiplierFilterSearchV1", summary = "Factory multiplier filter search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory multiplier filter list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryMultiplierFilterSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_multiplier_filter/search/v1")
    public ResponseEntity<?> factoryMultiplierFilterSearchV1(
            @MapperContextBinding(roots = FactoryMultiplierFilterRestDTOMapper.class, response = FactoryMultiplierFilterSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryMultiplierFilterSearchRqDTOv1 request) {
        FactoryMultiplierFilterSearchRsDTOv1 rs = new FactoryMultiplierFilterSearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryMultiplierFilterEntity> multiplierFilter = factoryMultiplierFilterSearchService
                    .findFactoryMultiplierFilters(factoryMultiplierFilerSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setMultiplierFilters(factoryMultiplierFilterRestDTOMapper.convertCollection(multiplierFilter.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(multiplierFilter))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryMultiplierFilterViewV1", summary = "Factory multiplier filter search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory multiplier filter data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryMultiplierFilterViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/factory_multiplier_filter/{multiplierId}/v1")
    public ResponseEntity<?> factoryMultiplierFilterViewV1(
            @MapperContextBinding(roots = FactoryMultiplierFilterRestDTOMapper.class, response = FactoryMultiplierFilterViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.MULTIPLIER_ID) @PathVariable("multiplierId") UUID multiplierId) {
        FactoryMultiplierFilterViewRsDTOv1 rs = new FactoryMultiplierFilterViewRsDTOv1();
        try {
            TwinFactoryMultiplierFilterEntity multiplierFilter = factoryMultiplierFilterService.findEntitySafe(multiplierId);

            rs
                    .setMultiplierFilter(factoryMultiplierFilterRestDTOMapper.convert(multiplierFilter, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
