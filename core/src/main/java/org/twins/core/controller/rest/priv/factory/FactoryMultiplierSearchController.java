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
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryMultiplierSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierSearchRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierViewRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryMultiplierRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryMultiplierSearchRqDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryMultiplierSearchService;
import org.twins.core.service.factory.FactoryMultiplierService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.MULTIPLIER_MANAGE, Permissions.MULTIPLIER_VIEW})
public class FactoryMultiplierSearchController extends ApiController {

    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryMultiplierSearchService factoryMultiplierSearchService;
    private final FactoryMultiplierRestDTOMapper factoryMultiplierRestDTOMapper;
    private final FactoryMultiplierSearchRqDTOReverseMapper factoryMultiplierSearchRqDTOReverseMapper;
    private final FactoryMultiplierService factoryMultiplierService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryMultiplierSearchV1", summary = "Factory multiplier search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory multiplier list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryMultiplierSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_multiplier/search/v1")
    public ResponseEntity<?> factoryMultiplierSearchV1(
            @MapperContextBinding(roots = FactoryMultiplierRestDTOMapper.class, response = FactoryMultiplierSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryMultiplierSearchRqDTOv1 request) {
        FactoryMultiplierSearchRsDTOv1 rs = new FactoryMultiplierSearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryMultiplierEntity> multipliers = factoryMultiplierSearchService
                    .findFactoryMultipliers(factoryMultiplierSearchRqDTOReverseMapper.convert(request), pagination);
            rs
                    .setMultipliers(factoryMultiplierRestDTOMapper.convertCollection(multipliers.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(multipliers))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryMultiplierViewV1", summary = "Factory multiplier view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory multiplier", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryMultiplierViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/factory_multiplier/{multiplierId}/v1")
    public ResponseEntity<?> factoryMultiplierViewV1(
            @MapperContextBinding(roots = FactoryMultiplierRestDTOMapper.class, response = FactoryMultiplierViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.MULTIPLIER_ID) @PathVariable("multiplierId") UUID multiplierId) {
        FactoryMultiplierViewRsDTOv1 rs = new FactoryMultiplierViewRsDTOv1();
        try {
            TwinFactoryMultiplierEntity multiplier = factoryMultiplierService.findEntitySafe(multiplierId);
            rs
                    .setMultiplier(factoryMultiplierRestDTOMapper.convert(multiplier, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
