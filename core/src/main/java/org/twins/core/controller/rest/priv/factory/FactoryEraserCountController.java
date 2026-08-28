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
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryEraserCountRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryEraserCountRsDTOv1;
import org.twins.core.enums.sort.FactoryEraserGroupField;
import org.twins.core.mappers.rest.factory.FactoryEraserCountRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryEraserSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryEraserSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_ERASER_MANAGE, Permissions.FACTORY_ERASER_VIEW})
public class FactoryEraserCountController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final FactoryEraserSearchDTOReverseMapper factoryEraserSearchDTOReverseMapper;
    private final FactoryEraserCountRestDTOMapper factoryEraserCountRestDTOMapper;
    private final FactoryEraserSearchService factoryEraserSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryEraserCountV1", summary = "Count factory erasers by group fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory eraser count", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryEraserCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_eraser/count/v1")
    public ResponseEntity<?> factoryEraserCountV1(
            @MapperContextBinding(roots = FactoryEraserCountRestDTOMapper.class, response = FactoryEraserCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryEraserCountRqDTOv1 request) {
        FactoryEraserCountRsDTOv1 rs = new FactoryEraserCountRsDTOv1();
        try {
            PaginationResult<CountResult<TwinFactoryEraserEntity, FactoryEraserGroupField>> result = factoryEraserSearchService
                    .countByGroupFields(factoryEraserSearchDTOReverseMapper.convert(request.getSearch()), request.getGroupFields(), pagination);
            rs
                    .setCounts(factoryEraserCountRestDTOMapper.convertCollection(result.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(result))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
