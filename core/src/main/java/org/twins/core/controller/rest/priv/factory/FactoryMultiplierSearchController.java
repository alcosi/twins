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
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dto.rest.factory.FactoryMultiplierSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierSearchRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryMultiplierRestDTOMapperV2;
import org.twins.core.mappers.rest.factory.FactoryMultiplierSearchRqDTOReverseMapper;
import org.twins.core.mappers.rest.factory.FactoryPipelineRestDTOMapperV2;
import org.twins.core.mappers.rest.factory.FactoryPipelineSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryMultiplierSearchService;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FactoryMultiplierSearchController extends ApiController {

    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryMultiplierSearchService factoryMultiplierSearchService;
    private final FactoryMultiplierRestDTOMapperV2 factoryMultiplierRestDTOMapperV2;
    private final FactoryMultiplierSearchRqDTOReverseMapper factoryMultiplierSearchRqDTOReverseMapper;


    @ParametersApiUserHeaders
    @Operation(operationId = "factoryMultiplierSearchV1", summary = "Factory multiplier search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory multiplier list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryMultiplierSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_multiplier/search/v1")
    public ResponseEntity<?> factoryMultiplierSearchV1(
            @MapperContextBinding(roots = FactoryMultiplierRestDTOMapperV2.class, response = FactoryMultiplierSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryMultiplierSearchRqDTOv1 request) {
        FactoryMultiplierSearchRsDTOv1 rs = new FactoryMultiplierSearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryMultiplierEntity> multipliers = factoryMultiplierSearchService
                    .findFactoryMultipliers(factoryMultiplierSearchRqDTOReverseMapper.convert(request), pagination);
            rs
                    .setMultipliers(factoryMultiplierRestDTOMapperV2.convertCollection(multipliers.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(multipliers))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
