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
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dto.rest.factory.FactoryConditionSetSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryConditionSetSearchRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryConditionSetSearchRqDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.permission.FactoryConditionSetRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryConditionSetSearchService;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FactoryConditionSetSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryConditionSetSearchRqDTOReverseMapper factoryConditionSetSearchRqDTOReverseMapper;
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;
    private final FactoryConditionSetSearchService factoryConditionSetSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryConditionSetSearchV1", summary = "Condition set search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Condition set list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryConditionSetSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_condition_set/search/v1")
    public ResponseEntity<?> factoryConditionSetSearchV1(
            @MapperContextBinding(roots = FactoryConditionSetRestDTOMapper.class, response = FactoryConditionSetSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryConditionSetSearchRqDTOv1 request) {
        FactoryConditionSetSearchRsDTOv1 rs = new FactoryConditionSetSearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryConditionSetEntity> conditionSetList = factoryConditionSetSearchService
                    .findFactoryConditionSets(factoryConditionSetSearchRqDTOReverseMapper.convert(request), pagination);
            rs
                    .setConditionSets(factoryConditionSetRestDTOMapper.convertCollection(conditionSetList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(conditionSetList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
