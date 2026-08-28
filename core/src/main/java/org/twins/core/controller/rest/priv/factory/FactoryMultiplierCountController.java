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
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryMultiplierCountRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierCountRsDTOv1;
import org.twins.core.enums.sort.FactoryMultiplierGroupField;
import org.twins.core.mappers.rest.factory.FactoryMultiplierCountRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryMultiplierSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryMultiplierSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_MULTIPLIER_MANAGE, Permissions.FACTORY_MULTIPLIER_VIEW})
public class FactoryMultiplierCountController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final FactoryMultiplierSearchDTOReverseMapper factoryMultiplierSearchDTOReverseMapper;
    private final FactoryMultiplierCountRestDTOMapper factoryMultiplierCountRestDTOMapper;
    private final FactoryMultiplierSearchService factoryMultiplierSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryMultiplierCountV1", summary = "Count factory multipliers by group fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory multiplier count", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryMultiplierCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_multiplier/count/v1")
    public ResponseEntity<?> factoryMultiplierCountV1(
            @MapperContextBinding(roots = FactoryMultiplierCountRestDTOMapper.class, response = FactoryMultiplierCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryMultiplierCountRqDTOv1 request) {
        FactoryMultiplierCountRsDTOv1 rs = new FactoryMultiplierCountRsDTOv1();
        try {
            PaginationResult<CountResult<TwinFactoryMultiplierEntity, FactoryMultiplierGroupField>> result = factoryMultiplierSearchService
                    .countByGroupFields(factoryMultiplierSearchDTOReverseMapper.convert(request.getSearch()), request.getGroupFields(), pagination);
            rs
                    .setCounts(factoryMultiplierCountRestDTOMapper.convertCollection(result.getList(), mapperContext))
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
