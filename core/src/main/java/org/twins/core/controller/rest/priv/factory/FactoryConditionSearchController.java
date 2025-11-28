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
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.dto.rest.factory.FactoryConditionSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryConditionSearchRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryConditionRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryConditionSearchRqDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryConditionSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.CONDITION_SET_MANAGE, Permissions.CONDITION_SET_VIEW})
public class FactoryConditionSearchController extends ApiController {

    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryConditionSearchRqDTOReverseMapper factoryConditionSearchRqDTOReverseMapper;
    private final FactoryConditionRestDTOMapper factoryConditionRestDTOMapper;
    private final FactoryConditionSearchService factoryConditionSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryConditionSearchV1", summary = "Conditions search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conditions list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryConditionSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_condition/search/v1")
    public ResponseEntity<?> factoryConditionSearchV1(
            @MapperContextBinding(roots = FactoryConditionRestDTOMapper.class, response = FactoryConditionSearchRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryConditionSearchRqDTOv1 request) {

        FactoryConditionSearchRsDTOv1 rs = new FactoryConditionSearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryConditionEntity> conditionList = factoryConditionSearchService
                    .findFactoryConditions(factoryConditionSearchRqDTOReverseMapper.convert(request), pagination);
            rs
                    .setPagination(paginationMapper.convert(conditionList))
                    .setConditions(factoryConditionRestDTOMapper.convertCollection(conditionList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);

    }
}
