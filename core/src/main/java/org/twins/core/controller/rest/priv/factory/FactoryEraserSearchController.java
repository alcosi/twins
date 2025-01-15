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
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dto.rest.factory.FactoryEraserSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryEraserSearchRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryEraserRestDTOMapperV2;
import org.twins.core.mappers.rest.factory.FactoryEraserSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryEraserSearchService;


@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FactoryEraserSearchController extends ApiController {

    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryEraserSearchDTOReverseMapper factoryEraserSearchDTOReverseMapper;
    private final FactoryEraserSearchService factoryEraserSearchService;
    private final FactoryEraserRestDTOMapperV2 factoryEraserRestDTOMapperV2;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryEraserSearchV1", summary = "Factory eraser search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory eraser data list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryEraserSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_eraser/search/v1")
    public ResponseEntity<?> factoryEraserSearchV1(
            @MapperContextBinding(roots = FactoryEraserRestDTOMapperV2.class, response = FactoryEraserSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryEraserSearchRqDTOv1 request) {
        FactoryEraserSearchRsDTOv1 rs = new FactoryEraserSearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryEraserEntity> eraserList = factoryEraserSearchService
                    .findFactoryEraser(factoryEraserSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setErasers(factoryEraserRestDTOMapperV2.convertCollection(eraserList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(eraserList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
