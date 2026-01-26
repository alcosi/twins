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
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryEraserSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryEraserSearchRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryEraserViewRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryEraserRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryEraserSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryEraserSearchService;
import org.twins.core.service.factory.FactoryEraserService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;


@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.ERASER_MANAGE, Permissions.ERASER_VIEW})
public class FactoryEraserSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryEraserSearchDTOReverseMapper factoryEraserSearchDTOReverseMapper;
    private final FactoryEraserSearchService factoryEraserSearchService;
    private final FactoryEraserRestDTOMapper factoryEraserRestDTOMapper;
    private final FactoryEraserService factoryEraserService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryEraserSearchV1", summary = "Factory eraser search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory eraser data list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryEraserSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_eraser/search/v1")
    public ResponseEntity<?> factoryEraserSearchV1(
            @MapperContextBinding(roots = FactoryEraserRestDTOMapper.class, response = FactoryEraserSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryEraserSearchRqDTOv1 request) {
        FactoryEraserSearchRsDTOv1 rs = new FactoryEraserSearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryEraserEntity> eraserList = factoryEraserSearchService
                    .findFactoryEraser(factoryEraserSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setErasers(factoryEraserRestDTOMapper.convertCollection(eraserList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(eraserList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }


    @ParametersApiUserHeaders
    @Operation(operationId = "factoryEraserViewV1", summary = "Factory eraser view by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory eraser data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryEraserViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/factory_eraser/{eraserId}/v1")
    public ResponseEntity<?> factoryEraserViewV1(
            @MapperContextBinding(roots = FactoryEraserRestDTOMapper.class, response = FactoryEraserViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @Parameter(example = DTOExamples.FACTORY_ERASER_ID) @PathVariable("eraserId") UUID eraserId) {
        FactoryEraserViewRsDTOv1 rs = new FactoryEraserViewRsDTOv1();
        try {
            TwinFactoryEraserEntity eraser = factoryEraserService.findEntitySafe(eraserId);
            rs
                    .setEraser(factoryEraserRestDTOMapper.convert(eraser, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
