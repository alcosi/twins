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
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dto.rest.factory.FactorySearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactorySearchRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryRestDTOMapperV2;
import org.twins.core.mappers.rest.factory.FactorySearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactorySearchService;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FactorySearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final FactorySearchDTOReverseMapper factorySearchDTOReverseMapper;
    private final FactoryRestDTOMapperV2 factoryRestDTOMapperV2;
    private final FactorySearchService factorySearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factorySearchListV1", summary = "Return a list of all factories for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactorySearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory/search/v1")
    public ResponseEntity<?> factorySearchListV1(
            @MapperContextBinding(roots = FactoryRestDTOMapperV2.class, response = FactorySearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactorySearchRqDTOv1 request) {
        FactorySearchRsDTOv1 rs = new FactorySearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryEntity> factoryList = factorySearchService
                    .findFactoriesInDomain(factorySearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setFactories(factoryRestDTOMapperV2.convertCollection(factoryList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(factoryList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}