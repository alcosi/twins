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
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactorySearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactorySearchRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryViewRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactorySearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactorySearchService;
import org.twins.core.service.factory.FactoryService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_MANAGE, Permissions.FACTORY_VIEW})
public class FactorySearchController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final FactorySearchDTOReverseMapper factorySearchDTOReverseMapper;
    private final FactoryRestDTOMapper factoryRestDTOMapper;
    private final FactorySearchService factorySearchService;
    private final FactoryService factoryService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factorySearchListV1", summary = "Return a list of all factories for the current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactorySearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory/search/v1")
    public ResponseEntity<?> factorySearchListV1(
            @MapperContextBinding(roots = FactoryRestDTOMapper.class, response = FactorySearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactorySearchRqDTOv1 request) {
        FactorySearchRsDTOv1 rs = new FactorySearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryEntity> factoryList = factorySearchService
                    .findFactoriesInDomain(factorySearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setFactories(factoryRestDTOMapper.convertCollection(factoryList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(factoryList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryViewV1", summary = "Factory data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/factory/{factoryId}/v1")
    public ResponseEntity<?> factoryViewV1(
            @MapperContextBinding(roots = FactoryRestDTOMapper.class, response = FactoryViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACTORY_ID) @PathVariable("factoryId") UUID factoryId) {
        FactoryViewRsDTOv1 rs = new FactoryViewRsDTOv1();
        try {
            TwinFactoryEntity factory = factoryService.findEntitySafe(factoryId);

            rs
                    .setFactory(factoryRestDTOMapper.convert(factory, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
