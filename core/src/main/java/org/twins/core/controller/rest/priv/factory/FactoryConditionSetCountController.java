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
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryConditionSetCountRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryConditionSetCountRsDTOv1;
import org.twins.core.enums.sort.FactoryConditionSetGroupField;
import org.twins.core.mappers.rest.factory.FactoryConditionSetCountRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryConditionSetSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryConditionSetSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_CONDITION_SET_MANAGE, Permissions.FACTORY_CONDITION_SET_VIEW})
public class FactoryConditionSetCountController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final FactoryConditionSetSearchDTOReverseMapper factoryConditionSetSearchDTOReverseMapper;
    private final FactoryConditionSetCountRestDTOMapper factoryConditionSetCountRestDTOMapper;
    private final FactoryConditionSetSearchService factoryConditionSetSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryConditionSetCountV1", summary = "Count factory condition sets by group fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Condition set count", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryConditionSetCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_condition_set/count/v1")
    public ResponseEntity<?> factoryConditionSetCountV1(
            @MapperContextBinding(roots = FactoryConditionSetCountRestDTOMapper.class, response = FactoryConditionSetCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryConditionSetCountRqDTOv1 request) {
        FactoryConditionSetCountRsDTOv1 rs = new FactoryConditionSetCountRsDTOv1();
        try {
            PaginationResult<CountResult<TwinFactoryConditionSetEntity, FactoryConditionSetGroupField>> result = factoryConditionSetSearchService
                    .countByGroupFields(factoryConditionSetSearchDTOReverseMapper.convert(request.getSearch()), request.getGroupFields(), pagination);
            rs
                    .setCounts(factoryConditionSetCountRestDTOMapper.convertCollection(result.getList(), mapperContext))
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
