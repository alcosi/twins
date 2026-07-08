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
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterCountRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterCountRsDTOv1;
import org.twins.core.enums.sort.FactoryMultiplierFilterGroupField;
import org.twins.core.mappers.rest.factory.FactoryMultiplierFilterCountRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryMultiplierFilterSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryMultiplierFilterSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_MULTIPLIER_MANAGE, Permissions.FACTORY_MULTIPLIER_VIEW})
public class FactoryMultiplierFilterCountController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final FactoryMultiplierFilterSearchDTOReverseMapper factoryMultiplierFilterSearchDTOReverseMapper;
    private final FactoryMultiplierFilterCountRestDTOMapper factoryMultiplierFilterCountRestDTOMapper;
    private final FactoryMultiplierFilterSearchService factoryMultiplierFilterSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryMultiplierFilterCountV1", summary = "Count factory multiplier filters by group fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory multiplier filter count", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryMultiplierFilterCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_multiplier_filter/count/v1")
    public ResponseEntity<?> factoryMultiplierFilterCountV1(
            @MapperContextBinding(roots = FactoryMultiplierFilterCountRestDTOMapper.class, response = FactoryMultiplierFilterCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryMultiplierFilterCountRqDTOv1 request) {
        FactoryMultiplierFilterCountRsDTOv1 rs = new FactoryMultiplierFilterCountRsDTOv1();
        try {
            PaginationResult<CountResult<TwinFactoryMultiplierFilterEntity, FactoryMultiplierFilterGroupField>> result = factoryMultiplierFilterSearchService
                    .countByGroupFields(factoryMultiplierFilterSearchDTOReverseMapper.convert(request.getSearch()), request.getGroupFields(), pagination);
            rs
                    .setCounts(factoryMultiplierFilterCountRestDTOMapper.convertCollection(result.getList(), mapperContext))
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
